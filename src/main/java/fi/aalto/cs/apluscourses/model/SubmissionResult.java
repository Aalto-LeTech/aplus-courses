package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.JsonUtil;
import fi.aalto.cs.apluscourses.utils.parser.FeedbackParser;
import fi.aalto.cs.apluscourses.utils.parser.O1FeedbackParser;
import fi.aalto.cs.apluscourses.utils.parser.S2FeedbackParser;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class SubmissionResult implements Browsable {

  public SubmissionFileInfo[] getFilesInfo() {
    return filesInfo;
  }

  public int getTestsSucceeded() {
    return testsSucceeded;
  }

  public int getTestsFailed() {
    return testsFailed;
  }

  public enum Status {
    UNKNOWN,
    GRADED,
    UNOFFICIAL,
    WAITING
  }

  private final long submissionId;

  private final int points;

  @NotNull
  private final Status status;

  private final double latePenalty;

  @NotNull
  private final Exercise exercise;

  private final SubmissionFileInfo @NotNull [] filesInfo;

  private final int testsSucceeded;

  private final int testsFailed;

  /**
   * Construct an instance with the given ID and exercise URL.
   */
  public SubmissionResult(long submissionId,
                          int points,
                          double latePenalty,
                          @NotNull Status status,
                          @NotNull Exercise exercise) {
    this(submissionId, points, latePenalty, status, exercise, new SubmissionFileInfo[0], -1, -1);
  }

  /**
   * Construct an instance with the given ID and exercise URL.
   */
  public SubmissionResult(long submissionId,
                          int points,
                          double latePenalty,
                          @NotNull Status status,
                          @NotNull Exercise exercise,
                          SubmissionFileInfo @NotNull [] filesInfo,
                          int testsSucceeded,
                          int testsFailed) {
    this.submissionId = submissionId;
    this.points = points;
    this.latePenalty = latePenalty;
    this.status = status;
    this.exercise = exercise;
    this.filesInfo = filesInfo;
    this.testsSucceeded = testsSucceeded;
    this.testsFailed = testsFailed;
  }

  /**
   * Construct a {@link SubmissionResult} instance from the given JSON object. The JSON object must
   * contain an integer for the "id" key, and optionally a string value for the "status" key.
   */
  @NotNull
  public static SubmissionResult fromJsonObject(@NotNull JSONObject jsonObject,
                                                @NotNull Exercise exercise,
                                                @NotNull Course course) {
    long id = jsonObject.getLong("id");
    int points = jsonObject.getInt("grade");
    double latePenalty = jsonObject.optDouble("late_penalty_applied", 0.0);

    Status status = Status.UNKNOWN;
    String statusString = jsonObject.optString("status");
    if ("ready".equals(statusString)) {
      status = Status.GRADED;
    } else if ("unofficial".equals(statusString)) {
      status = Status.UNOFFICIAL;
    } else if ("waiting".equals(statusString)) {
      status = Status.WAITING;
    }

    var filesInfo = JsonUtil.parseArray(
        jsonObject.getJSONArray("files"),
        JSONArray::getJSONObject,
        SubmissionFileInfo::fromJsonObject,
        SubmissionFileInfo[]::new
    );

    FeedbackParser feedbackParser = new FeedbackParser();

    if (course.getFeedbackParser() != null && exercise.isSubmittable() && jsonObject.has("feedback")) {
      switch (course.getFeedbackParser()) {
        case O1FeedbackParser.NAME:
          feedbackParser = new O1FeedbackParser();
          break;
        case S2FeedbackParser.NAME:
          feedbackParser = new S2FeedbackParser();
          break;
        default:
      }
    }

    var testResults = feedbackParser.parseTestResults(jsonObject.optString("feedback", ""));

    return new SubmissionResult(id, points, latePenalty, status, exercise, filesInfo, testResults.succeeded,
        testResults.failed);
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

  public double getLatePenalty() {
    return latePenalty;
  }

  @Override
  public @NotNull String getHtmlUrl() {
    return exercise.getHtmlUrl() + "submissions/" + submissionId + "/";
  }

  public @NotNull Exercise getExercise() {
    return exercise;
  }
}
