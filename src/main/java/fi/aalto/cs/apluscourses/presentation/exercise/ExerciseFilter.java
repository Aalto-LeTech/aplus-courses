package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.presentation.filter.TypedFilter;

public abstract class ExerciseFilter extends TypedFilter<ExerciseViewModel> {
  protected ExerciseFilter() {
    super(ExerciseViewModel.class);
  }

  public static class NonSubmittableFilter extends ExerciseFilter {
    @Override
    public boolean applyInternal(ExerciseViewModel item) {
      return !item.isDummy() && !item.isSubmittable();
    }
  }

  public static class CompletedFilter extends ExerciseFilter {
    @Override
    public boolean applyInternal(ExerciseViewModel item) {
      return !item.isDummy() && item.getModel().isCompleted();
    }
  }

  public static class OptionalFilter extends ExerciseFilter {
    @Override
    public boolean applyInternal(ExerciseViewModel item) {
      return !item.isDummy() && item.getModel().isOptional();
    }
  }
}
