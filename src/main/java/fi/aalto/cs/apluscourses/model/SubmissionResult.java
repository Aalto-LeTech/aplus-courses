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

  @NotNull
  private final Status status;

  @NotNull
  private final String exerciseUrl;

  /**
   * Construct an instance with the given ID and exercise URL.
   */
  public SubmissionResult(long submissionId, @NotNull Status status, @NotNull String exerciseUrl) {
    this.submissionId = submissionId;
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

    String exerciseUrl = jsonObject.getJSONObject("exercise").getString("html_url");

    Status status = Status.UNKNOWN;
    String statusString = jsonObject.optString("status");
    if ("ready".equals(statusString)) {
      status = Status.GRADED;
    } else if ("unofficial".equals(statusString)) {
      status = Status.UNOFFICIAL;
    }

    return new SubmissionResult(id, status, exerciseUrl);
  }

  public long getId() {
    return submissionId;
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
