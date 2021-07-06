package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.ExerciseNotSelectedNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingFileNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.FileDoesNotExistException;
import fi.aalto.cs.apluscourses.model.FileFinder;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.SubmissionFileInfo;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.DownloadSubmissionViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class DownloadSubmissionAction extends AnAction {
  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Interfaces.AuthenticationProvider authenticationProvider;

  @NotNull
  private final FileFinder fileFinder;

  @NotNull
  private final Dialogs dialogs;

  @NotNull
  private final Notifier notifier;

  // TODO: store language and default group ID in the object model and read them from there
  private final Interfaces.LanguageSource languageSource;

  @NotNull
  private final Interfaces.AssistantModeProvider assistantModeProvider;

  @NotNull
  private final ComponentInstaller.Factory componentInstallerFactory;

  @NotNull
  private final InstallerDialogs.Factory dialogsFactory;


  /**
   * Constructor with reasonable defaults.
   */
  public DownloadSubmissionAction() {
    this(
        PluginSettings.getInstance(),
        project -> Optional.ofNullable(PluginSettings.getInstance().getCourseProject(project))
            .map(CourseProject::getAuthentication).orElse(null),
        VfsUtil::findFileInDirectory,
        Dialogs.DEFAULT,
        new DefaultNotifier(),
        project -> PluginSettings.getInstance().getCourseFileManager(project).getLanguage(),
        () -> PluginSettings.getInstance().isAssistantMode(),
        new ComponentInstallerImpl.FactoryImpl<>(new SimpleAsyncTaskManager()),
        InstallerDialogs::new
    );
  }


  /**
   * Construct an exercise submission action with the given parameters. This constructor is useful
   * for testing purposes.
   */
  public DownloadSubmissionAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                  @NotNull Interfaces.AuthenticationProvider authenticationProvider,
                                  @NotNull FileFinder fileFinder,
                                  @NotNull Dialogs dialogs,
                                  @NotNull Notifier notifier,
                                  @NotNull Interfaces.LanguageSource languageSource,
                                  @NotNull Interfaces.AssistantModeProvider assistantModeProvider,
                                  @NotNull ComponentInstaller.Factory componentInstallerFactory,
                                  @NotNull InstallerDialogs.Factory dialogsFactory) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.authenticationProvider = authenticationProvider;
    this.fileFinder = fileFinder;
    this.dialogs = dialogs;
    this.notifier = notifier;
    this.languageSource = languageSource;
    this.assistantModeProvider = assistantModeProvider;
    this.componentInstallerFactory = componentInstallerFactory;
    this.dialogsFactory = dialogsFactory;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getProject();
    var courseViewModel = mainViewModelProvider.getMainViewModel(project).courseViewModel.get();
    var exercisesTreeViewModel = mainViewModelProvider.getMainViewModel(project).exercisesViewModel.get();
    if (project == null || courseViewModel == null || exercisesTreeViewModel == null) {
      return;
    }
    var selectedItem = exercisesTreeViewModel.getSelectedItem();
    if (!(selectedItem instanceof SubmissionResultViewModel)) {
      return;
    }
    var submissionId = selectedItem.getId();

    BaseTreeViewModel.Selection selection = exercisesTreeViewModel.findSelected();
    ExerciseViewModel selectedExercise = (ExerciseViewModel) selection.getLevel(2);
    ExerciseGroupViewModel selectedExerciseGroup = (ExerciseGroupViewModel) selection.getLevel(1);
    if (selectedExercise == null || selectedExerciseGroup == null) {
      notifier.notifyAndHide(new ExerciseNotSelectedNotification(), project);
      return;
    }

    Exercise exercise = selectedExercise.getModel();
    Course course = courseViewModel.getModel();

    String language = languageSource.getLanguage(project);

    Map<String, String> exerciseModules =
        courseViewModel.getModel().getExerciseModules().get(exercise.getId());

    Optional<String> moduleName = Optional
        .ofNullable(exerciseModules)
        .map(self -> self.get(language));


    Module selectedModule = moduleName.map(course::getModuleByName).orElse(null);

    var downloadSubmissionViewModel = new DownloadSubmissionViewModel(course, selectedModule, submissionId, project);
    if (!dialogs.create(downloadSubmissionViewModel, project).showAndGet()) {
      return;
    }

    var module = downloadSubmissionViewModel.selectedModule.get();
    var newName = downloadSubmissionViewModel.moduleName.get();

    if (module == null || newName == null) {
      return;
    }

    var moduleCopy = module.copy(newName);
    var refresher = RefreshQueue.getInstance().createSession(true, true,
        () -> downloadFiles(e, ((SubmissionResult) selectedItem.getModel()).getFilesInfo(), moduleCopy));
    var moduleVf = LocalFileSystem.getInstance().findFileByIoFile(moduleCopy.getFullPath().toFile());
    if (moduleVf != null) {
      refresher.addFile(moduleVf);
    }
    componentInstallerFactory.getInstallerFor(course, dialogsFactory.getDialogs(e.getProject()))
        .installAsync(List.of(moduleCopy), refresher::launch);
  }

  private void downloadFiles(@NotNull AnActionEvent e,
                             SubmissionFileInfo @NotNull [] submissionFilesInfo,
                             @NotNull Module module) {
    var project = e.getProject();
    var courseViewModel = mainViewModelProvider.getMainViewModel(project).courseViewModel.get();
    if (courseViewModel == null || project == null) {
      return;
    }
    var course = courseViewModel.getModel();
    course.validate();

    for (var info : submissionFilesInfo) {
      try {
        var file = fileFinder.findFile(module.getFullPath(), info.getFileName()).toFile();
        var auth = authenticationProvider.getAuthentication(project);
        CoursesClient.fetch(new URL(info.getUrl()), file, auth);
        var vf = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (vf != null) {
          new OpenFileDescriptor(project, vf).navigate(true);
        }
      } catch (FileDoesNotExistException ex) {
        notifier.notifyAndHide(new MissingFileNotification(module.getPath(), info.getFileName(), true), project);
      } catch (IOException ex) {
        notifier.notifyAndHide(new NetworkErrorNotification(ex), project);
      }
    }

  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setVisible(assistantModeProvider.isAssistantMode());
    e.getPresentation().setEnabled(false);
    var courseViewModel = mainViewModelProvider.getMainViewModel(e.getProject()).courseViewModel.get();
    var exercisesTreeViewModel = mainViewModelProvider.getMainViewModel(e.getProject()).exercisesViewModel.get();
    if (courseViewModel != null && exercisesTreeViewModel != null) {
      BaseTreeViewModel.Selection selection = exercisesTreeViewModel.findSelected();
      ExerciseViewModel selectedExercise = (ExerciseViewModel) selection.getLevel(2);
      var selectedItem = exercisesTreeViewModel.getSelectedItem();
      e.getPresentation().setEnabled(selectedItem instanceof SubmissionResultViewModel
          && exercisesTreeViewModel.isAuthenticated()
          && selectedExercise != null
          && selectedExercise.isSubmittable());
    }
  }
}
