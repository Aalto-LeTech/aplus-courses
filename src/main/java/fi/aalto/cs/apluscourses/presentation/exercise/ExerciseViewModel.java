package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.DummyExercise;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.TutorialExercise;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ExerciseViewModel extends SelectableNodeViewModel<Exercise> implements Searchable {

  /**
   * Construct a view model corresponding to the given exercise.
   */
  public ExerciseViewModel(@NotNull Exercise exercise) {
    super(exercise, exercise.isSubmittable() ? CollectionUtil.mapWithIndex(
        exercise.getSubmissionResults(), SubmissionResultViewModel::new, 1) : List.of());
    if (exercise.isSubmittable()) {
      getChildren().add(new SubmitExerciseViewModel());
    }
    Collections.reverse(getChildren());
  }

  public String getPresentableName() {
    return getModel().getName();
  }

  @Override
  public @NotNull String getSearchableString() {
    return getPresentableName();
  }

  /**
   * Returns {@code true} if the exercise is submittable from the plugin, {@code false} otherwise.
   */
  public boolean isSubmittable() {
    return getModel().isSubmittable();
  }

  public boolean isDummy() {
    return getModel().isDummy();
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
    IN_GRADING,
    LATE,
    TUTORIAL,
    DUMMY
  }

  /**
   * Returns a {@link Status} that indicates the points status of this exercise.
   */
  public Status getStatus() {
    Exercise exercise = getModel();
    if (exercise.isDummy()) {
      return Status.DUMMY;
    } else if (exercise.isInGrading()) {
      return Status.IN_GRADING;
    } else if (exercise instanceof TutorialExercise) {
      return Status.TUTORIAL;
    } else if (exercise.isOptional()) {
      return Status.OPTIONAL_PRACTICE;
    } else if (exercise.getSubmissionResults().isEmpty()) {
      return Status.NO_SUBMISSIONS;
    } else if (exercise.getUserPoints() == exercise.getMaxPoints()) {
      return Status.FULL_POINTS;
    } else if (exercise.isLate()) {
      return Status.LATE;
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
    if ("Feedback".equals(getPresentableName()) || "Palaute".equals(getPresentableName())) { // O1_SPECIFIC
      return "";
    }
    if (getStatus() == Status.OPTIONAL_PRACTICE) {
      return "optional practice";
    }
    Exercise exercise = getModel();
    if (exercise instanceof DummyExercise) {
      return "???";
    }
    String lateString = exercise.isLate() ? "late, " : "";
    return lateString + exercise.getSubmissionResults().size() + " of "
        + exercise.getMaxSubmissions() + ", " + exercise.getUserPoints() + "/"
        + exercise.getMaxPoints();
  }
}
