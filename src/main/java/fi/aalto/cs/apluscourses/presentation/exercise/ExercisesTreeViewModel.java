package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ExercisesTreeViewModel extends BaseTreeViewModel<List<ExerciseGroup>> {

  private boolean isEmptyTextVisible = false;

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public ExercisesTreeViewModel(@NotNull List<ExerciseGroup> exerciseGroups,
                                @NotNull Options filterOptions) {
    super(exerciseGroups,
        exerciseGroups
            .stream()
            .map(ExerciseGroupViewModel::new)
            .collect(Collectors.toList()),
        filterOptions);
  }

  public boolean isEmptyTextVisible() {
    return isEmptyTextVisible;
  }

  public void setEmptyTextVisible(boolean emptyTextVisible) {
    isEmptyTextVisible = emptyTextVisible;
  }
}
