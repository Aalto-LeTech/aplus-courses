package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class SubmissionResult {

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
  private final String exerciseUrl;

  /**
   * Construct an instance with the given ID and exercise URL.
   */
  public SubmissionResult(long submissionId,
                          int points,
                          @NotNull String exerciseUrl,
                          @NotNull Status status) {
    this.submissionId = submissionId;
    this.points = points;
    this.status = status;
    this.exerciseUrl = exerciseUrl;
  }

  /**
   * Construct a {@link SubmissionResult} instance from the given JSON object. The JSON object must
   * contain an integer for the "id" key, a JSON object for the "exercise" key, and optionally a
   * string value for the "status" key.
   */
  @NotNull
  public static SubmissionResult fromJsonObject(@NotNull JSONObject jsonObject) {
    long id = jsonObject.getLong("id");
    int points = jsonObject.getInt("grade");
    String exerciseUrl = jsonObject.getJSONObject("exercise").getString("html_url");

    Status status = Status.UNKNOWN;
    String statusString = jsonObject.optString("status");
    if ("ready".equals(statusString)) {
      status = Status.GRADED;
    } else if ("unofficial".equals(statusString)) {
      status = Status.UNOFFICIAL;
    }

    return new SubmissionResult(id, points, exerciseUrl, status);
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

  @NotNull
  public String getUrl() {
    return exerciseUrl + "submissions/" + submissionId + "/";
  }

}
