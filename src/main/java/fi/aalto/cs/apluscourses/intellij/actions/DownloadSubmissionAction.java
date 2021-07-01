package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.model.ProjectModuleSource;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.FileFinder;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class DownloadSubmissionAction extends AnAction {
  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final SubmitExerciseAction.AuthenticationProvider authenticationProvider;

  @NotNull
  private final FileFinder fileFinder;

  @NotNull
  private final ProjectModuleSource moduleSource;

  @NotNull
  private final Dialogs dialogs;

  @NotNull
  private final Notifier notifier;


  // TODO: store language and default group ID in the object model and read them from there
  private final SubmitExerciseAction.LanguageSource languageSource;

  @NotNull
  private final AssistantModeProvider assistantModeProvider;

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
        new ProjectModuleSource(),
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
                                  @NotNull SubmitExerciseAction.AuthenticationProvider authenticationProvider,
                                  @NotNull FileFinder fileFinder,
                                  @NotNull ProjectModuleSource moduleSource,
                                  @NotNull Dialogs dialogs,
                                  @NotNull Notifier notifier,
                                  @NotNull SubmitExerciseAction.LanguageSource languageSource,
                                  @NotNull AssistantModeProvider assistantModeProvider,
                                  @NotNull ComponentInstaller.Factory componentInstallerFactory,
                                  @NotNull InstallerDialogs.Factory dialogsFactory) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.authenticationProvider = authenticationProvider;
    this.fileFinder = fileFinder;
    this.moduleSource = moduleSource;
    this.dialogs = dialogs;
    this.notifier = notifier;
    this.languageSource = languageSource;
    this.assistantModeProvider = assistantModeProvider;
    this.componentInstallerFactory = componentInstallerFactory;
    this.dialogsFactory = dialogsFactory;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    CourseViewModel courseViewModel = mainViewModelProvider.getMainViewModel(e.getProject()).courseViewModel.get();
    var exercisesTreeViewModel = mainViewModelProvider.getMainViewModel(e.getProject()).exercisesViewModel.get();
    if (courseViewModel != null && exercisesTreeViewModel != null) {
      var selectedItem = exercisesTreeViewModel.getSelectedItem();
      if (!(selectedItem instanceof SubmissionResultViewModel)) {
        return;
      }
      var submissionId = selectedItem.getId();
      System.out.println(courseViewModel.getModules().getElementAt(0).getName());
      var adventure = courseViewModel.getModules().getElementAt(0).getModel();
      var newName = submissionId + "_" + adventure.getName();
      var adventureCopy = adventure.copy(newName);
      System.out.println(adventureCopy.getName());
      Course course = courseViewModel.getModel();
      componentInstallerFactory.getInstallerFor(course, dialogsFactory.getDialogs(e.getProject()))
          .installAsync(List.of(adventureCopy), course::validate);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setVisible(assistantModeProvider.isAssistantMode());
    e.getPresentation().setEnabled(false);
    CourseViewModel courseViewModel = mainViewModelProvider.getMainViewModel(e.getProject()).courseViewModel.get();
    var exercisesTreeViewModel = mainViewModelProvider.getMainViewModel(e.getProject()).exercisesViewModel.get();
    if (courseViewModel != null && exercisesTreeViewModel != null) {
      var selectedItem = exercisesTreeViewModel.getSelectedItem();
      e.getPresentation().setEnabled(selectedItem instanceof SubmissionResultViewModel
          && exercisesTreeViewModel.isAuthenticated());
    }
  }

  @FunctionalInterface
  public interface AssistantModeProvider {
    boolean isAssistantMode();
  }
}
