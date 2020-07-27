package fi.aalto.cs.apluscourses.model;

public class SubmissionResult {

  private long submissionId;
  private int submissionNumber;

  /**
   * Construct an instance with the given ID and submission number.
   */
  public SubmissionResult(long submissionId, int submissionNumber) {
    this.submissionId = submissionId;
    this.submissionNumber = submissionNumber;
  }

  public long getId() {
    return submissionId;
  }

  public int getSubmissionNumber() {
    return submissionNumber;
  }

}
