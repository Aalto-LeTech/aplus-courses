package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.FeedbackParser;
import java.util.HashMap;
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

  private HashMap<String, Integer> testResults = new HashMap<>();

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
   * contain an integer for the "id" key, a string for the "feedback" key, and optionally a string
   * value for the "status" key.
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

    SubmissionResult submissionResult = new SubmissionResult(id, points, status, exercise);

    if (exercise.isOptional()) {
      String feedback = jsonObject.getString("feedback");
      submissionResult.setTestResults(feedback);
    }

    return submissionResult;
  }

  private void setExerciseCompleted() {
    if (!testResults.isEmpty()
            && testResults.get("failed") == 0
            && testResults.get("canceled") == 0) {
      exercise.setCompleted(true);
    }
  }

  public void setTestResults(HashMap<String, Integer> results) {
    this.testResults = results;
    setExerciseCompleted();
  }

  public void setTestResults(String feedback) {
    this.testResults = FeedbackParser.testResultsMap(feedback).orElse(this.testResults);
    setExerciseCompleted();
  }

  public HashMap<String, Integer> getTestResults() {
    return testResults;
  }

  /**
   * Returns a string containing either the points of the submission,
   * or the test results, if they exist.
   */
  public String getFeedbackString() {
    return testResults.isEmpty()
        ? String.format("%1$d/%2$d", points, exercise.getMaxPoints())
        : FeedbackParser.testResultString(testResults);
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
    return exercise.getHtmlUrl() + "submissions/" + submissionId + "/";
  }

  public Exercise getExercise() {
    return exercise;
  }
}
