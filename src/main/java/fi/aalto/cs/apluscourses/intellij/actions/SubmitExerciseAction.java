package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import org.jetbrains.annotations.NotNull;

public class SubmitExerciseAction extends AnAction {

  public static final String ACTION_ID = SubmitExerciseAction.class.getCanonicalName();

  @NotNull
  private MainViewModelProvider mainViewModelProvider;

  public SubmitExerciseAction() {
    this(PluginSettings.getInstance());
  }

  public SubmitExerciseAction(@NotNull MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(e.getProject());
    ExercisesTreeViewModel exercisesTree = mainViewModel.exercisesViewModel.get();
    if (exercisesTree == null) {
      return;
    }
    ExerciseViewModel exercise = exercisesTree.getSelectedExercise();
    if (exercise == null) {
      return;
    }

    Messages.showInfoMessage(
        e.getProject(),
        "Submitting exercise " + exercise.getPresentableName() + ".",
        "Exercise Submission");
  }
}
