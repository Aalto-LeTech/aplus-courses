package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.presentation.filter.TypedFilter;

public abstract class ExerciseGroupFilter extends TypedFilter<ExerciseGroupViewModel> {
  public ExerciseGroupFilter() {
    super(ExerciseGroupViewModel.class);
  }

  public static class ClosedFilter extends ExerciseGroupFilter {
    @Override
    public boolean applyInternal(ExerciseGroupViewModel item) {
      return !item.getModel().isOpen();
    }
  }
}
