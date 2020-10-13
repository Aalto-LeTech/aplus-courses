package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import org.jetbrains.annotations.NotNull;

public class ExerciseViewModel extends SelectableNodeViewModel<Exercise> {

  /**
   * Construct a view model corresponding to the given exercise.
   */
  public ExerciseViewModel(@NotNull Exercise exercise) {
    super(exercise, CollectionUtil.mapWithIndex(
        exercise.getSubmissionResults(), SubmissionResultViewModel::new, 1));
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(getModel().getName());
  }

  /**
   * Returns {@code true} if the exercise is submittable from the plugin, {@code false} otherwise.
   */
  public boolean isSubmittable() {
    // O1_SPECIFIC
    String name = getPresentableName();
    int maxSubmissions = getModel().getMaxSubmissions();
    return name.length() > "Assignment xx (".length() && !"Assignment 1 (Piazza)".equals(name)
        && !"Assignment  debugger".equals(name) && (maxSubmissions == 10 || maxSubmissions == 0);
  }

  @Override
  public long getId() {
    return getModel().getId();
  }

  public enum Status {
    OPTIONAL_PRACTICE,
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
    if (exercise.getMaxSubmissions() == 0 && exercise.getMaxPoints() == 0) {
      return Status.OPTIONAL_PRACTICE;
    } else if (exercise.getSubmissionResults().isEmpty()) {
      return Status.NO_SUBMISSIONS;
    } else if (exercise.getUserPoints() == exercise.getMaxPoints()) {
      return Status.FULL_POINTS;
    } else if (exercise.getUserPoints() == 0) {
      return Status.NO_POINTS;
    } else {
      return Status.PARTIAL_POINTS;
    }
  }

  /**
   * Returns a text describing the status of the exercise (points and number of submissions).
   */
  @NotNull
  public String getStatusText() {
    if ("Feedback".equals(getPresentableName())) { // O1_SPECIFIC
      return "";
    }
    if (getStatus() == Status.OPTIONAL_PRACTICE) {
      return "optional practice";
    }
    Exercise exercise = getModel();
    return "" + exercise.getSubmissionResults().size() + " of " + exercise.getMaxSubmissions()
        + ", " + exercise.getUserPoints() + "/" + exercise.getMaxPoints();
  }
}
