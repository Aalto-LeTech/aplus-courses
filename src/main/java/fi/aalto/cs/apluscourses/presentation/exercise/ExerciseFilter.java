package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.presentation.filter.TypedFilter;

public abstract class ExerciseFilter extends TypedFilter<ExerciseViewModel> {
  public ExerciseFilter() {
    super(ExerciseViewModel.class);
  }

  public static class NonSubmittableFilter extends ExerciseFilter {
    @Override
    public boolean applyInternal(ExerciseViewModel item) {
      return !item.isSubmittable();
    }
  }

  public static class CompletedFilter extends ExerciseFilter {
    @Override
    public boolean applyInternal(ExerciseViewModel item) {
      return item.isCompleted();
    }
  }
}
