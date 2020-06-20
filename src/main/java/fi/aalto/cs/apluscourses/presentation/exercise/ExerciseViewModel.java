package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class ExerciseViewModel {

  @NotNull
  private final Exercise exercise;

  public ExerciseViewModel(@NotNull Exercise exercise) {
    this.exercise = exercise;
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(exercise.getName());
  }

  private static final Pattern assignmentNumberPattern = Pattern.compile("Assignment[ ]+([0-9]+)");

  /**
   * Parses the assignment number from the name of the exercise. Note, that this is a hacky solution
   * that assumes that the exercise name contains the string {@code "Assignment x"}. Therefore, it
   * will not work for courses that don't follow that convention for exercise names.
   * @return The parsed assignment number, or -1 if no assignment number could be parsed.
   */
  public int getAssignmentNumber() {
    Matcher matcher = assignmentNumberPattern.matcher(getPresentableName());
    if (matcher.find()) {
      try {
        return Integer.parseInt(matcher.group(1));
      } catch (NumberFormatException e) {
        return -1;
      }
    }
    return -1;
  }
}
