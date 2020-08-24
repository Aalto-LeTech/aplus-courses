package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesTreeViewModel extends BaseViewModel<List<ExerciseGroup>>
    implements TreeViewModel {

  @NotNull
  private final List<ExerciseGroupViewModel> groupViewModels;

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public ExercisesTreeViewModel(@NotNull List<ExerciseGroup> exerciseGroups) {
    super(exerciseGroups);
    this.groupViewModels = exerciseGroups
        .stream()
        .map(ExerciseGroupViewModel::new)
        .collect(Collectors.toList());
  }

  /**
   * Returns the exercise from this tree that is selected, or null if no exercise is selected.
   */
  @Nullable
  public ExerciseViewModel getSelectedExercise() {
    return getGroupViewModels()
        .stream()
        .flatMap(group -> group.getExerciseViewModels().stream())
        .filter(ExerciseViewModel::isSelected)
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns the submission from this tree that is selected, or null if no submission is selected.
   */
  @Nullable
  public SubmissionResultViewModel getSelectedSubmission() {
    return getGroupViewModels()
        .stream()
        .flatMap(group -> group.getExerciseViewModels().stream())
        .flatMap(exercise -> exercise.getSubmissionResultViewModels().stream())
        .filter(SubmissionResultViewModel::isSelected)
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns the exercise group where the selected exercise belongs, or null if no exercise is
   * selected.
   */
  @Nullable
  public ExerciseGroupViewModel getSelectedExerciseGroup() {
    for (ExerciseGroupViewModel exerciseGroupViewModel : getGroupViewModels()) {
      for (ExerciseViewModel exerciseViewModel : exerciseGroupViewModel.getExerciseViewModels()) {
        if (exerciseViewModel.isSelected()) {
          return exerciseGroupViewModel;
        }
      }
    }
    return null;
  }

  @NotNull
  public List<ExerciseGroupViewModel> getGroupViewModels() {
    return groupViewModels;
  }

  @Nullable
  @Override
  public List<ExerciseGroupViewModel> getSubtrees() {
    return getGroupViewModels();
  }
}
