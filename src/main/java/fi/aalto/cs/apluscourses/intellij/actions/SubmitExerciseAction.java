package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.ExerciseNotSelectedNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingFileNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingModuleNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NotSubmittableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SuccessfulSubmissionNotification;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.FileDoesNotExistException;
import fi.aalto.cs.apluscourses.model.FileFinder;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmitExerciseAction extends AnAction {

  public static final String ACTION_ID = SubmitExerciseAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final FileFinder fileFinder;

  @NotNull
  private final ModuleSource moduleSource;

  @NotNull
  private final Dialogs dialogs;

  @NotNull
  private final Notifier notifier;

  /**
   * Constructor with reasonable defaults.
   */
  public SubmitExerciseAction() {
    this(
        PluginSettings.getInstance(),
        VfsUtil::findFileInDirectory,
        DefaultModuleSource.INSTANCE,
        Dialogs.DEFAULT,
        Notifications.Bus::notify
    );
  }

  /**
   * Construct an exercise submission action with the given parameters. This constructor is useful
   * for testing purposes.
   */
  public SubmitExerciseAction(@NotNull MainViewModelProvider mainViewModelProvider,
                              @NotNull FileFinder fileFinder,
                              @NotNull ModuleSource moduleSource,
                              @NotNull Dialogs dialogs,
                              @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.fileFinder = fileFinder;
    this.moduleSource = moduleSource;
    this.dialogs = dialogs;
    this.notifier = notifier;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    Authentication authentication = mainViewModel.authentication.get();

    e.getPresentation().setEnabled(project != null && exercisesViewModel != null
        && authentication != null && courseViewModel != null);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    try {
      trySubmit(project);
    } catch (IOException ex) {
      notifyNetworkError(ex, project);
    } catch (FileDoesNotExistException ex) {
      notifier.notify(new MissingFileNotification(ex.getPath(), ex.getName()), project);
    } catch (ModuleMissingException ex) {
      notifier.notify(new MissingModuleNotification(ex.getModuleName()), project);
    }
  }

  private void trySubmit(@NotNull Project project)
      throws IOException, FileDoesNotExistException, ModuleMissingException {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    Authentication authentication = mainViewModel.authentication.get();

    if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
      return;
    }

    ExerciseViewModel selectedExercise = exercisesViewModel.getSelectedExercise();
    if (selectedExercise == null) {
      notifier.notify(new ExerciseNotSelectedNotification(), project);
      return;
    }

    Exercise exercise = selectedExercise.getModel();

    Course course = courseViewModel.getModel();
    ExerciseDataSource exerciseDataSource = course.getExerciseDataSource();

    SubmissionInfo submissionInfo = exerciseDataSource.getSubmissionInfo(exercise, authentication);
    if (!submissionInfo.isSubmittable()) {
      notifier.notify(new NotSubmittableNotification(), project);
      return;
    }

    Map<String, String> exerciseModules =
        courseViewModel.getModel().getExerciseModules().get(exercise.getId());

    Optional<String> moduleName = Optional.ofNullable(exerciseModules).map(self -> self.get("en"));

    Module selectedModule;
    if (moduleName.isPresent()) {
      selectedModule = moduleName
          .map(self -> moduleSource.getModule(project, self))
          .orElseThrow(() -> new ModuleMissingException(moduleName.get()));
    } else {
      Module[] modules = moduleSource.getModules(project);

      ModuleSelectionViewModel moduleSelectionViewModel = new ModuleSelectionViewModel(modules);
      if (!dialogs.create(moduleSelectionViewModel, project).showAndGet()) {
        return;
      }
      selectedModule = moduleSelectionViewModel.selectedModule.get();
    }

    if (selectedModule == null) {
      return;
    }

    Path modulePath = Paths.get(ModuleUtilCore.getModuleDirPath(selectedModule));
    Map<String, Path> files = new HashMap<>();
    for (SubmittableFile file : submissionInfo.getFiles()) {
      files.put(file.getKey(), fileFinder.findFile(modulePath, file.getName()));
    }

    SubmissionHistory history = exerciseDataSource.getSubmissionHistory(exercise, authentication);

    List<Group> groups = new ArrayList<>(exerciseDataSource.getGroups(course, authentication));
    groups.add(0, new Group(0, Collections
        .singletonList(getText("ui.toolWindow.subTab.exercises.submission.submitAlone"))));

    SubmissionViewModel submission =
        new SubmissionViewModel(exercise, submissionInfo, history, groups, files);

    if (!dialogs.create(submission, project).showAndGet()) {
      return;
    }

    exerciseDataSource.submit(submission.buildSubmission(), authentication);
    notifier.notify(new SuccessfulSubmissionNotification(), project);
  }

  private void notifyNetworkError(@NotNull IOException exception, @Nullable Project project) {
    notifier.notify(new NetworkErrorNotification(exception), project);
  }

  public interface ModuleSource {

    @NotNull
    Module[] getModules(@NotNull Project project);

    @Nullable
    Module getModule(@NotNull Project project, @NotNull String moduleName);
  }

  private static class DefaultModuleSource implements ModuleSource {

    public static final ModuleSource INSTANCE = new DefaultModuleSource();

    @Override
    @NotNull
    public Module[] getModules(@NotNull Project project) {
      return ModuleManager.getInstance(project).getModules();
    }

    @Override
    @Nullable
    public Module getModule(@NotNull Project project, @NotNull String moduleName) {
      return ModuleManager.getInstance(project).findModuleByName(moduleName);
    }
  }

  private static class ModuleMissingException extends Exception {

    @NotNull
    private final String moduleName;

    public ModuleMissingException(@NotNull String moduleName) {
      this.moduleName = moduleName;
    }

    @NotNull
    public String getModuleName() {
      return moduleName;
    }
  }
}
