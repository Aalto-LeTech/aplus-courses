package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.presentation.filter.Option;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.presentation.filter.TypedFilter;

public class ExerciseFilterOptions extends Options {

  //TODO UI strings
  public ExerciseFilterOptions() {
    super(new Option("Non-submittable", null, new NonSubmittableFilter()),
        new Option("No submissions left", null, new NoSubmissionsLeftFilter()));
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

  public static class NoSubmissionsLeftFilter extends ExerciseFilter {
    @Override
    public boolean applyInternal(ExerciseViewModel item) {
      return item.getModel().hasMaxSubmissionsBeenExceeded();
    }
  }
}
