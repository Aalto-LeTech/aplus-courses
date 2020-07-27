package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExerciseViewModel extends SelectableNodeViewModel<Exercise> implements TreeViewModel {

  public ExerciseViewModel(@NotNull Exercise exercise) {
    super(exercise);
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(getModel().getName());
  }

  /**
   * Returns {@code true} if the exercise is submittable from the plugin, {@code false} otherwise.
   */
  public boolean isSubmittable() {
    String presentableName = getPresentableName();
    return presentableName.length() > "Assignment xx (".length()
        && !"Assignment 1 (Piazza)".equals(presentableName);
  }

  public enum Status {
    NO_SUBMISSIONS,
    NO_POINTS,
    PARTIAL_POINTS,
    FULL_POINTS,
  }

  /**
   * Returns a {@link Status} that indicates the points status of this exercise.
   */
  public Status getStatus() {
    Exercise exercise = getModel();
    if (exercise.getSubmissionResults().isEmpty()) {
      return Status.NO_SUBMISSIONS;
    } else if (exercise.getUserPoints() == exercise.getMaxPoints()) {
      return Status.FULL_POINTS;
    } else if (exercise.getUserPoints() == 0) {
      return Status.NO_POINTS;
    } else {
      return Status.PARTIAL_POINTS;
    }
  }

  @Nullable
  @Override
  public List<TreeViewModel> getSubtrees() {
    return getModel()
        .getSubmissionResults()
        .stream()
        .map(SubmissionResultViewModel::new)
        .collect(Collectors.toList());
  }
}
