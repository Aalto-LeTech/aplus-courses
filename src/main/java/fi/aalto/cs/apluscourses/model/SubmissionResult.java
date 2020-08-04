package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class SubmissionResult {

  private final long submissionId;
  @NotNull
  private final String exerciseUrl;

  /**
   * Construct an instance with the given ID and exercise URL.
   */
  public SubmissionResult(long submissionId, @NotNull String exerciseUrl) {
    this.submissionId = submissionId;
    this.exerciseUrl = exerciseUrl;
  }

  public long getId() {
    return submissionId;
  }

  @NotNull
  public String getUrl() {
    return exerciseUrl + "submissions/" + submissionId + "/";
  }

}
