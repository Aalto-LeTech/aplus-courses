package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

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
