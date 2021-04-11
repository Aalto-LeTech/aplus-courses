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
import org.jetbrains.annotations.NotNull;

public class TutorialAction extends DumbAwareAction {

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Notifier notifier;

  private Project project;

  public TutorialAction() {
    this(PluginSettings.getInstance(),
         new DefaultNotifier());
  }

  public TutorialAction(MainViewModelProvider mainViewModelProvider,
                        Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.notifier = notifier;
  }

  //Similar to SubmitExerciseAction, get the selected Exercise
  //TODO Make the Action's icon appear conditionally only when a Tutorial is selected?
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    this.project = project;
    confirmStart(project);
  }

  private void confirmStart(@NotNull Project project) {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    Authentication authentication = mainViewModel.authentication.get();

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

    //Display a window asking for confirmation to Start the Tutorial
    Tutorial tutorial = ((TutorialExercise) selectedExercise.getModel()).getTutorial();
    //tutorial.tutorialUpdated.addListener(this, TutorialAction::completeTutorial);
    TutorialViewModel viewModel = new TutorialViewModel(tutorial, project);
    mainViewModelProvider.getMainViewModel(project).tutorialViewModel.set(viewModel);
    viewModel.startTutorial();

    //Reset tutorialViewModel to null in MainVIewMOdel
    //reset also the Tasks to unfinished
    //TODO polish this class a bit
  }

  public void completeTutorial() {
    mainViewModelProvider.getMainViewModel(project).tutorialViewModel.set(null);
  }
}
