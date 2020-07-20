package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingFileNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingModuleNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NotSubmittableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.exercise.ModuleSelectionDialog;
import fi.aalto.cs.apluscourses.ui.exercise.SubmissionDialog;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.CalledWithReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmitExerciseAction extends AnAction {

  public static final String ACTION_ID = SubmitExerciseAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final SubmissionInfoFactory submissionInfoFactory;

  @NotNull
  private final SubmissionHistoryFactory submissionHistoryFactory;

  @NotNull
  private final GroupsFactory groupsFactory;

  @NotNull
  private final FileFinder fileFinder;

  @NotNull
  private final ModuleSelectionDialog.Factory moduleDialogFactory;

  @NotNull
  private final SubmissionDialog.Factory submissionDialogFactory;

  @NotNull
  private final Notifier notifier;


  /**
   * Constructor with reasonable defaults.
   */
  public SubmitExerciseAction() {
    this(
        PluginSettings.getInstance(),
        SubmissionInfo::forExercise,
        SubmissionHistory::forExercise,
        Group::getGroups,
        VfsUtil::findFileInDirectory,
        viewModel -> new ModuleSelectionDialog(viewModel),
        viewModel -> new SubmissionDialog(viewModel),
        Notifications.Bus::notify
    );
  }

  /**
   * Construct an exercise submission action with the given parameters. This constructor is useful
   * for testing purposes.
   */
  public SubmitExerciseAction(@NotNull MainViewModelProvider mainViewModelProvider,
                              @NotNull SubmissionInfoFactory submissionInfoFactory,
                              @NotNull SubmissionHistoryFactory submissionHistoryFactory,
                              @NotNull GroupsFactory groupsFactory,
                              @NotNull FileFinder fileFinder,
                              @NotNull ModuleSelectionDialog.Factory moduleDialogFactory,
                              @NotNull SubmissionDialog.Factory submissionDialogFactory,
                              @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.submissionInfoFactory = submissionInfoFactory;
    this.submissionHistoryFactory = submissionHistoryFactory;
    this.groupsFactory = groupsFactory;
    this.fileFinder = fileFinder;
    this.moduleDialogFactory = moduleDialogFactory;
    this.submissionDialogFactory = submissionDialogFactory;
    this.notifier = notifier;
  }

  @FunctionalInterface
  public interface SubmissionInfoFactory {
    SubmissionInfo fromExercise(@NotNull Exercise exercise,
                                @NotNull APlusAuthentication authentication) throws IOException;
  }

  @FunctionalInterface
  public interface SubmissionHistoryFactory {
    SubmissionHistory fromExercise(@NotNull Exercise exercise,
                                   @NotNull APlusAuthentication authentication) throws IOException;
  }

  @FunctionalInterface
  public interface GroupsFactory {
    List<Group> getAvailableGroups(@NotNull Course course,
                                   @NotNull APlusAuthentication authentication) throws IOException;
  }

  @FunctionalInterface
  public interface FileFinder {
    @Nullable
    Path findFile(@NotNull Path directory, @NotNull String filename);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(e.getProject());
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesTreeViewModel
        = mainViewModel.exercisesViewModel.get();
    APlusAuthenticationViewModel authenticationViewModel
        = mainViewModel.authenticationViewModel.get();

    e.getPresentation().setEnabled(e.getProject() != null && exercisesTreeViewModel != null
        && authenticationViewModel != null && courseViewModel != null
        && courseViewModel.getModel() != null);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(e.getProject());
    ExerciseViewModel selectedExercise
        = mainViewModel.exercisesViewModel.get().getSelectedExercise();
    if (selectedExercise == null) {
      return;
    }
    Exercise exercise = selectedExercise.getModel();

    APlusAuthentication authentication
        = mainViewModel.authenticationViewModel.get().getAuthentication();
    Course course = mainViewModel.courseViewModel.get().getModel();
    Project project = e.getProject();

    SubmissionInfo submissionInfo = tryGetSubmissionInfo(
        selectedExercise.getModel(), authentication, project);
    if (submissionInfo == null) {
      return;
    }

    SubmissionHistory submissionHistory = tryGetSubmissionHistory(
        exercise, authentication, project);
    if (submissionHistory == null) {
      return;
    }

    List<Group> groups = tryGetUserGroups(course, authentication, project);
    if (groups == null) {
      return;
    }

    Module[] modules = Optional.ofNullable(project)
        .map(ModuleManager::getInstance)
        .map(ModuleManager::getModules)
        .orElseGet(() -> new Module[0]);
    Module selectedModule;
    String moduleName = tryGetExerciseModuleName(course, exercise);
    if (moduleName == null) {
      selectedModule = tryGetModuleFromDialog(modules, project);
    } else {
      selectedModule = tryGetModuleFromName(modules, moduleName, project);
    }
    if (selectedModule == null) {
      return;
    }

    SubmissionViewModel submissionViewModel = new SubmissionViewModel(
        exercise, submissionInfo, submissionHistory, groups, authentication, project);

    List<Path> filePaths = tryGetFilePaths(submissionInfo.getFilenames(), selectedModule, project);
    if (filePaths == null) {
      return;
    }

    if (submissionDialogFactory.createDialog(submissionViewModel).showAndGet()) {
      // Do actual submission
    }
  }

  @Nullable
  private SubmissionInfo tryGetSubmissionInfo(@NotNull Exercise exercise,
                                              @NotNull APlusAuthentication authentication,
                                              @Nullable Project project) {
    try {
      SubmissionInfo submissionInfo
          = submissionInfoFactory.fromExercise(exercise, authentication);
      if (submissionInfo.getFilenames().isEmpty()) {
        notifier.notify(new NotSubmittableNotification(), project);
        return null;
      }
      return submissionInfo;
    } catch (IOException e) {
      notifyNetworkError(e, project);
      return null;
    }
  }

  @Nullable
  private SubmissionHistory tryGetSubmissionHistory(@NotNull Exercise exercise,
                                                    @NotNull APlusAuthentication authentication,
                                                    @Nullable Project project) {
    try {
      return submissionHistoryFactory.fromExercise(exercise, authentication);
    } catch (IOException e) {
      notifyNetworkError(e, project);
      return null;
    }
  }

  @Nullable
  private List<Group> tryGetUserGroups(@NotNull Course course,
                                       @NotNull APlusAuthentication authentication,
                                       @Nullable Project project) {
    try {
      return groupsFactory.getAvailableGroups(course, authentication);
    } catch (IOException e) {
      notifyNetworkError(e, project);
      return null;
    }
  }

  @Nullable
  private String tryGetExerciseModuleName(@NotNull Course course,
                                          @NotNull Exercise exercise) {
    Map<String, String> exerciseModules = course.getExerciseModules().get(exercise.getId());
    if (exerciseModules == null) {
      return null;
    }

    return exerciseModules.get("en");
  }

  @CalledWithReadLock
  @Nullable
  private Module tryGetModuleFromDialog(@NotNull Module[] modules, @Nullable Project project) {
    ModuleSelectionViewModel viewModel = new ModuleSelectionViewModel(modules, project);
    if (!moduleDialogFactory.createDialog(viewModel).showAndGet()) {
      return null;
    }
    return viewModel.getSelectedModule();
  }

  @CalledWithReadLock
  @Nullable
  private Module tryGetModuleFromName(@NotNull Module[] modules,
                                      @NotNull String moduleName,
                                      @Nullable Project project) {
    Optional<Module> match = Arrays
        .stream(modules)
        .filter(module -> moduleName.equals(module.getName()))
        .findFirst();
    if (match.isPresent()) {
      return match.get();
    }
    notifier.notify(new MissingModuleNotification(moduleName), project);
    return null;
  }

  @CalledWithReadLock
  @Nullable
  private List<Path> tryGetFilePaths(@NotNull List<String> filenames,
                                     @NotNull Module module,
                                     @Nullable Project project) {
    Path modulePath = Paths.get(ModuleUtilCore.getModuleDirPath(module));
    List<Path> filePaths = new ArrayList<>(filenames.size());
    for (String filename : filenames) {
      Path filePath = fileFinder.findFile(modulePath, filename);
      if (filePath == null) {
        notifier.notify(new MissingFileNotification(module.getName(), filename), project);
        return null;
      }
      filePaths.add(filePath);
    }
    return filePaths;
  }

  private void notifyNetworkError(@NotNull IOException exception, @Nullable Project project) {
    notifier.notify(new NetworkErrorNotification(exception), project);
  }
}
