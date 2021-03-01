package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.List;
import org.jetbrains.annotations.NotNull;


public class EmptyExercisesTreeViewModel extends ExercisesTreeViewModel {

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public EmptyExercisesTreeViewModel(@NotNull List<ExerciseGroup> exerciseGroups,
                                     @NotNull Options filterOptions) {
    super(exerciseGroups, filterOptions);
  }

  @Override
  public boolean isEmptyTextVisible() {
    return true;
  }
}
