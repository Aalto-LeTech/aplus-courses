package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ExercisesTreeViewModel extends BaseTreeViewModel<List<ExerciseGroup>>
        implements Searchable {

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
