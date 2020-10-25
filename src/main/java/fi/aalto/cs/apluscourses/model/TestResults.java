package fi.aalto.cs.apluscourses.model;

import java.util.HashMap;

public class TestResults {

  private final int succeeded;
  private final int failed;
  private final int canceled;
  private final Long exerciseId;
  private final Long submissionId;

  /**
   * Construct an instance with the given test results and IDs.
   */
  public TestResults(int succeeded,
                     int failed,
                     int canceled,
                     Long exerciseId,
                     Long submissionId) {
    this.succeeded = succeeded;
    this.failed = failed;
    this.canceled = canceled;
    this.exerciseId = exerciseId;
    this.submissionId = submissionId;
  }

  /**
   * Getter for test results.
   * @return HashMap containing test results.
   */
  public HashMap<String, Integer> getTestResults() {
    HashMap<String, Integer> results = new HashMap<>();
    results.put("succeeded", this.succeeded);
    results.put("failed", this.failed);
    results.put("canceled", this.canceled);
    return results;
  }

  public Long getExerciseId() {
    return exerciseId;
  }

  public Long getSubmissionId() {
    return submissionId;
  }
}
