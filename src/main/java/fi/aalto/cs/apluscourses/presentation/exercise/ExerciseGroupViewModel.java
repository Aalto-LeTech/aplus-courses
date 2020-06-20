package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ExerciseGroupViewModel {

  @NotNull
  private final ExerciseGroup exerciseGroup;

  @NotNull
  private final List<ExerciseViewModel> sortedExercises;

  /**
   * Construct an exercise group view model with the given exercise group.
   */
  public ExerciseGroupViewModel(@NotNull ExerciseGroup exerciseGroup) {
    this.exerciseGroup = exerciseGroup;
    /*
     * The A+ API does not return the exercises in the same order as they are in the e-book, so we
     * sort them here based on their names. This is somewhat hacky and will not work for courses
     * that use a different naming convention. We also filter out feedback exercises, as we can't
     * put them in any reasonable place in the sorted list.
     */
    this.sortedExercises = exerciseGroup
        .getExercises()
        .values()
        .stream()
        .map(ExerciseViewModel::new)
        .filter(HARDCODED_O1_FILTER)
        .sorted(HARDCODED_O1_COMPARATOR)
        .collect(Collectors.toList());
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(exerciseGroup.getName());
  }

  @NotNull
  public List<ExerciseViewModel> getExerciseViewModels() {
    return sortedExercises;
  }

  private static Predicate<ExerciseViewModel> HARDCODED_O1_FILTER
      = exerciseViewModel -> !"Feedback".equals(exerciseViewModel.getPresentableName());

  private static Comparator<ExerciseViewModel> HARDCODED_O1_COMPARATOR
      = Comparator.comparingInt(ExerciseViewModel::getAssignmentNumber);

}
