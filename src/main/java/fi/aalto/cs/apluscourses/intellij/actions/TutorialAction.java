package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.ExerciseNotSelectedNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Tutorial;
import fi.aalto.cs.apluscourses.model.TutorialExercise;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialViewModel;
import fi.aalto.cs.apluscourses.ui.ideactivities.StartTutorialDialog;
import fi.aalto.cs.apluscourses.ui.ideactivities.TaskView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TutorialAction extends DumbAwareAction {

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final TutorialAuthenticationProvider authenticationProvider;

  @NotNull
  private final Notifier notifier;

  /**
   * Empty Constructor.
   */
  public TutorialAction() {
    this(PluginSettings.getInstance(), new DefaultNotifier(), project -> {
      var courseProject = PluginSettings.getInstance().getCourseProject(project);
      return courseProject == null ? null : courseProject.getAuthentication();
    });
  }

  /**
   *Constructor.
   * @param mainViewModelProvider mainViewModelProvider
   * @param notifier Notifier
   * @param authenticationProvider1 TutorialAuthenticationProvider
   */
  public TutorialAction(@NotNull MainViewModelProvider mainViewModelProvider,
                        @NotNull Notifier notifier,
                        @NotNull TutorialAuthenticationProvider authenticationProvider1) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.notifier = notifier;
    this.authenticationProvider = authenticationProvider1;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (e.getProject() == null) {
      return;
    }
    confirmStart(e.getProject());
  }

  private void confirmStart(@NotNull Project project) {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    Authentication authentication = authenticationProvider.getAuthentication(project);

    if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
      return;
    }

    BaseTreeViewModel.Selection selection = exercisesViewModel.findSelected();
    ExerciseViewModel selectedExercise = (ExerciseViewModel) selection.getLevel(2);
    ExerciseGroupViewModel selectedExerciseGroup = (ExerciseGroupViewModel) selection.getLevel(1);
    if (selectedExercise == null || selectedExerciseGroup == null
            || !ExerciseViewModel.Status.TUTORIAL.equals(selectedExercise.getStatus())) {
      notifier.notifyAndHide(new ExerciseNotSelectedNotification(), project);
      return;
    }
    Tutorial tutorial = ((TutorialExercise) selectedExercise.getModel()).getTutorial();
    if (mainViewModel.tutorialViewModel.get() != null) {
      mainViewModel.tutorialViewModel.get().cancelTutorial();
    }

    TutorialViewModel tutorialViewModel = new TutorialViewModel(tutorial,
            TaskView::createAndShow, project);
    mainViewModelProvider.getMainViewModel(project).tutorialViewModel.set(tutorialViewModel);
    CompleteTutorial completeTutorial = new CompleteTutorial(mainViewModel);
    tutorial.tutorialUpdated.addListener(completeTutorial, CompleteTutorial::completeTutorial);
    StartTutorialDialog.createAndShow(tutorialViewModel);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(e.getProject());
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    Authentication authentication = authenticationProvider.getAuthentication(e.getProject());
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    boolean isTutorialSelected =
            exercisesViewModel != null
            && authentication != null && courseViewModel != null
            && exercisesViewModel.getSelectedItem() != null
            && !(exercisesViewModel.getSelectedItem() instanceof ExerciseGroupViewModel)
            && exercisesViewModel.findSelected().getLevel(2) != null
            && ExerciseViewModel.Status.TUTORIAL.equals(
               ((ExerciseViewModel) exercisesViewModel.findSelected().getLevel(2)).getStatus());

    e.getPresentation().setVisible(e.getProject() != null && isTutorialSelected);
  }

  @FunctionalInterface
  public interface TutorialAuthenticationProvider {
    Authentication getAuthentication(@Nullable Project project);
  }


  private static class CompleteTutorial {
    private final MainViewModel mainViewModel;

    CompleteTutorial(MainViewModel mainViewModel) {
      this.mainViewModel = mainViewModel;
    }

    public void completeTutorial() {
      mainViewModel.tutorialViewModel.set(null);
    }
  }
}
