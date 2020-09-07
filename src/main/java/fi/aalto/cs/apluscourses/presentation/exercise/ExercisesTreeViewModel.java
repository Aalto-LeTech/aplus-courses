package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesTreeViewModel extends BaseTreeViewModel<List<ExerciseGroup>> {

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public ExercisesTreeViewModel(@NotNull List<ExerciseGroup> exerciseGroups,
                                @NotNull Executor filterExecutor) {
    super(exerciseGroups,
        exerciseGroups
            .stream()
            .map(ExerciseGroupViewModel::new)
            .collect(Collectors.toList()),
        new ExerciseFilterOptions(),
        filterExecutor);
  }

  /**
   * Returns the exercise from this tree that is selected, or null if no exercise is selected.
   */
  @Nullable
  public ExerciseViewModel getSelectedExercise() {
    return getChildren().parallelStream()
        .map(SelectableNodeViewModel::getChildren)
        .flatMap(List::parallelStream)
        .filter(SelectableNodeViewModel::isSelected)
        .map(ExerciseViewModel.class::cast)
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns the submission from this tree that is selected, or null if no submission is selected.
   */
  @Nullable
  public SubmissionResultViewModel getSelectedSubmission() {
    return getChildren().parallelStream()
        .map(SelectableNodeViewModel::getChildren)
        .flatMap(List::parallelStream)
        .map(SelectableNodeViewModel::getChildren)
        .flatMap(List::parallelStream)
        .filter(SelectableNodeViewModel::isSelected)
        .map(SubmissionResultViewModel.class::cast)
        .findFirst()
        .orElse(null);
  }
}
