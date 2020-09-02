package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.presentation.filter.Option;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.presentation.filter.TypedFilter;

public class ExerciseFilterOptions extends Options {

  public ExerciseFilterOptions() {
    super(new Option("Non-submittable",
            null,
            new ExerciseFilterOptions.NonSubmittableFilter()
        ),
        new Option("No more submissions",
            null,
            new ExerciseFilterOptions.StatusFilter(ExerciseViewModel.Status.NO_SUBMISSIONS)
        ));
  }

  public abstract static class ExerciseFilter extends TypedFilter<ExerciseViewModel> {
    public ExerciseFilter() {
      super(ExerciseViewModel.class);
    }
  }

  public static class NonSubmittableFilter extends ExerciseFilter {
    @Override
    public boolean applyInternal(ExerciseViewModel item) {
      return !item.isSubmittable();
    }
  }

  public static class StatusFilter extends ExerciseFilter {
    private final ExerciseViewModel.Status status;

    public StatusFilter(ExerciseViewModel.Status status) {
      this.status = status;
    }

    @Override
    public boolean applyInternal(ExerciseViewModel item) {
      return item.getStatus() == status;
    }
  }
}
