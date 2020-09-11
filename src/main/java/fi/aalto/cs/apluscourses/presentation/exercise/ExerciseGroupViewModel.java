package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ExerciseGroupViewModel extends SelectableNodeViewModel<ExerciseGroup> {

  /**
   * Construct an exercise group view model with the given exercise group.
   */
  public ExerciseGroupViewModel(@NotNull ExerciseGroup exerciseGroup) {
    super(exerciseGroup, exerciseGroup
        .getExercises()
        .values()
        .stream()
        .map(ExerciseViewModel::new)
        .sorted(EXERCISE_COMPARATOR) // O1_SPECIFIC
        .collect(Collectors.toList()));
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(getModel().getName());
  }

  // O1_SPECIFIC
  private static final Comparator<ExerciseViewModel> EXERCISE_COMPARATOR = (exercise1, exercise2)
      -> {
    int chapter1 = getExerciseChapter(exercise1);
    int chapter2 = getExerciseChapter(exercise2);
    if (chapter1 > chapter2) {
      return 1;
    }
    if (chapter2 > chapter1) {
      return -1;
    }

    // Feedback exercises are always last
    if ("Feedback".equals(exercise1.getPresentableName())) {
      return 1;
    }
    if ("Feedback".equals(exercise2.getPresentableName())) {
      return -1;
    }

    int number1 = getExerciseNumber(exercise1);
    int number2 = getExerciseNumber(exercise2);
    if (number1 > number2) {
      return 1;
    }
    if (number2 > number1) {
      return -1;
    }
    return 0;
  };

  private static final Pattern CHAPTER_REGEX = Pattern.compile("\\/w[0-9]+\\/ch([0-9]+)\\/");

  private static int getExerciseChapter(@NotNull ExerciseViewModel exercise) {
    String htmlUrl = exercise.getModel().getHtmlUrl();
    Matcher matcher = CHAPTER_REGEX.matcher(htmlUrl);
    matcher.find();
    try {
      return Integer.parseInt(matcher.group(1));
    } catch (IllegalStateException | NumberFormatException | IndexOutOfBoundsException e) {
      return -1;
    }
  }

  private static final Pattern NUMBER_REGEX = Pattern.compile("w[0-9]+_ch[0-9]+_([0-9]+)");

  private static int getExerciseNumber(@NotNull ExerciseViewModel exercise) {
    String htmlUrl = exercise.getModel().getHtmlUrl();
    Matcher matcher = NUMBER_REGEX.matcher(htmlUrl);
    matcher.find();
    try {
      return Integer.parseInt(matcher.group(1));
    } catch (IllegalStateException | NumberFormatException | IndexOutOfBoundsException e) {
      return -1;
    }
  }
}
