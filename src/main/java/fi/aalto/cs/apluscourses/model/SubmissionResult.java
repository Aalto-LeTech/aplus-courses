package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class SubmissionResult implements Browsable {

  public enum Status {
    UNKNOWN,
    GRADED,
    UNOFFICIAL
  }

  private final long submissionId;

  private final int points;

  @NotNull
  private final Status status;

  @NotNull
  private final Exercise exercise;

  /**
   * Construct an instance with the given ID and exercise URL.
   */
  public SubmissionResult(long submissionId,
                          int points,
                          @NotNull Status status,
                          @NotNull Exercise exercise) {
    this.submissionId = submissionId;
    this.points = points;
    this.status = status;
    this.exercise = exercise;
  }

  /**
   * Construct a {@link SubmissionResult} instance from the given JSON object. The JSON object must
   * contain an integer for the "id" key, and optionally a string value for the "status" key.
   */
  @NotNull
  public static SubmissionResult fromJsonObject(@NotNull JSONObject jsonObject,
                                                @NotNull Exercise exercise) {
    long id = jsonObject.getLong("id");
    int points = jsonObject.getInt("grade");

    Status status = Status.UNKNOWN;
    String statusString = jsonObject.optString("status");
    if ("ready".equals(statusString)) {
      status = Status.GRADED;
    } else if ("unofficial".equals(statusString)) {
      status = Status.UNOFFICIAL;
    }

    return new SubmissionResult(id, points, status, exercise);
  }

  public long getId() {
    return submissionId;
  }

  public int getPoints() {
    return points;
  }

  @NotNull
  public Status getStatus() {
    return status;
  }

  @Override
  public @NotNull String getHtmlUrl() {
    return exercise.getHtmlUrl() + "submissions/" + submissionId + "/";
  }

  public @NotNull Exercise getExercise() {
    return exercise;
  }
}
