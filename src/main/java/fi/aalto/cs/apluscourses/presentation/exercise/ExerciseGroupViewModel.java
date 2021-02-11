package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ExerciseGroupViewModel extends SelectableNodeViewModel<ExerciseGroup>
        implements Searchable {

  /**
   * Construct an exercise group view model with the given exercise group.
   */
  public ExerciseGroupViewModel(@NotNull ExerciseGroup exerciseGroup) {
    super(exerciseGroup, exerciseGroup
        .getExercises()
        .values()
        .stream()
        .map(ExerciseViewModel::new)
        .sorted(EXERCISE_COMPARATOR)
        .collect(Collectors.toList()));
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(getModel().getName());
  }

  private static final Comparator<ExerciseViewModel> EXERCISE_COMPARATOR = (exercise1, exercise2)
      -> {
    /*
     * Sorting lexicographically by the HTML URL almost works, except for the assignment numbers,
     * since "...w01_ch04_9/" > "...w01_ch04_10/". So we compare the HTML URLs without the
     * assignment numbers, and if they aren't equal, then there is a clear "winner". If they are
     * equal, we compare the HTML URLs by length first, so that "...10/" becomes greater than
     * "...9/". This conveniently also makes feedback exercises the last exercises in their
     * chapters.
     */
    String htmlUrl1 = exercise1.getModel().getHtmlUrl();
    String htmlUrl2 = exercise2.getModel().getHtmlUrl();
    String weekAndChapter1 = withoutExerciseNumber(htmlUrl1);
    String weekAndChapter2 = withoutExerciseNumber(htmlUrl2);
    int compared = weekAndChapter1.compareTo(weekAndChapter2);
    if (compared != 0) {
      return compared;
    }
    if (htmlUrl1.length() > htmlUrl2.length()) {
      return 1;
    }
    if (htmlUrl2.length() > htmlUrl1.length()) {
      return -1;
    }
    return htmlUrl1.compareTo(htmlUrl2);
  };

  private static String withoutExerciseNumber(@NotNull String htmlUrl) {
    int index = htmlUrl.lastIndexOf('_');
    return index != -1 ? htmlUrl.substring(0, index) : htmlUrl;
  }

  @Override
  protected boolean isHiddenIfNoVisibleChildren() {
    return this.getChildren().stream().noneMatch(SelectableNodeViewModel::isVisible);
  }

  @Override
  public long getId() {
    return getModel().getId();
  }
}
