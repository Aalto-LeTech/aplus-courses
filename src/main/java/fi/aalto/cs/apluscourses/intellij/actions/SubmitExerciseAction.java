package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingFileNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NotSubmittableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.FileDoesNotExistException;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmittableExercise;
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
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmitExerciseAction extends AnAction {

  public static final String ACTION_ID = SubmitExerciseAction.class.getCanonicalName();

  @NotNull
  private MainViewModelProvider mainViewModelProvider;

  @NotNull
  private Notifier notifier;

  public SubmitExerciseAction() {
    this(PluginSettings.getInstance(), Notifications.Bus::notify);
  }

  public SubmitExerciseAction(@NotNull MainViewModelProvider mainViewModelProvider,
                              @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.notifier = notifier;
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

    APlusAuthentication authentication
        = mainViewModel.authenticationViewModel.get().getAuthentication();
    Course course = mainViewModel.courseViewModel.get().getModel();
    Project project = e.getProject();

    SubmittableExercise exercise = tryGetSubmittableExercise(
        selectedExercise.getModel(), authentication, project);
    if (exercise == null) {
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

    ModuleSelectionViewModel moduleSelectionViewModel = new ModuleSelectionViewModel(modules);

    if (!new ModuleSelectionDialog(project, moduleSelectionViewModel).showAndGet()) {
      return;
    }

    Module selectedModule = moduleSelectionViewModel.selectedModule.get();

    if (selectedModule == null) {
      return;
    }


    Path basePath = Paths.get(ModuleUtilCore.getModuleDirPath(selectedModule));

    Path[] filePaths;
    try {
      filePaths = exercise.getFilePaths(basePath);
    } catch (FileDoesNotExistException ex) {
      notifier.notify(new MissingFileNotification(selectedModule.getName(), ex.getName()), project);
      return;
    }

    SubmissionViewModel viewModel = new SubmissionViewModel(
        exercise, submissionHistory, groups, authentication, selectedModule, filePaths);

    if (new SubmissionDialog(viewModel, project).showAndGet()) {
      viewModel.buildSubmission().submit();
    }
  }

  @Nullable
  private SubmittableExercise tryGetSubmittableExercise(@NotNull Exercise exercise,
                                                        @NotNull APlusAuthentication authentication,
                                                        @Nullable Project project) {
    try {
      SubmittableExercise submittableExercise = SubmittableExercise.fromExerciseId(
          exercise.getId(), authentication);
      if (submittableExercise.getFiles().isEmpty()) {
        notifier.notify(new NotSubmittableNotification(), project);
        return null;
      }
      return submittableExercise;
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
      return SubmissionHistory.getSubmissionHistory(exercise.getId(), authentication);
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
      return Group.getGroups(course, authentication);
    } catch (IOException e) {
      notifyNetworkError(e, project);
      return null;
    }
  }

  private void notifyNetworkError(@NotNull IOException exception, @Nullable Project project) {
    notifier.notify(new NetworkErrorNotification(exception), project);
  }
}
