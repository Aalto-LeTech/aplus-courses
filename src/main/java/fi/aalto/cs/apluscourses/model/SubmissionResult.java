package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class SubmissionResult {

  private final long submissionId;
  private final int submissionNumber;
  @NotNull
  private final String exerciseUrl;

  /**
   * Construct an instance with the given ID and submission number.
   */
  public SubmissionResult(long submissionId, int submissionNumber, @NotNull String exerciseUrl) {
    this.submissionId = submissionId;
    this.submissionNumber = submissionNumber;
    this.exerciseUrl = exerciseUrl;
  }

  public long getId() {
    return submissionId;
  }

  public int getSubmissionNumber() {
    return submissionNumber;
  }

  @NotNull
  public String getUrl() {
    return exerciseUrl + "submissions/" + submissionId + "/";
  }

}
