package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.presentation.filter.Option;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.presentation.filter.TypedFilter;

public class ExerciseFilterOptions extends Options {

  public ExerciseFilterOptions() {
    super(new Option(getText("presentation.exerciseFilterOptions.nonSubmittable"), null,
        new NonSubmittableFilter()));
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
}
