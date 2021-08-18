package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class Exercise implements Browsable {

  private final long id;

  @NotNull
  private final String name;

  @NotNull
  private final SubmissionInfo submissionInfo;

  @NotNull
  private final String htmlUrl;

  @NotNull
  private final List<SubmissionResult> submissionResults =
      Collections.synchronizedList(new ArrayList<>());

  private final int maxPoints;

  private final int maxSubmissions;

  @NotNull
  private final OptionalLong bestSubmissionId;

  /**
   * Construct an exercise instance with the given parameters.
   *
   * @param id               The ID of the exercise.
   * @param name             The name of the exercise.
   * @param htmlUrl          A URL to the HTML page of the exercise.
   * @param maxPoints        The maximum points available from this exercise.
   * @param maxSubmissions   The maximum number of submissions allowed for this exercise.
   * @param bestSubmissionId The ID of the best submission for the exercise if one exists.
   */
  public Exercise(long id,
                  @NotNull String name,
                  @NotNull String htmlUrl,
                  @NotNull SubmissionInfo submissionInfo,
                  int maxPoints,
                  int maxSubmissions,
                  @NotNull OptionalLong bestSubmissionId) {
    this.id = id;
    this.name = name;
    this.htmlUrl = htmlUrl;
    this.submissionInfo = submissionInfo;
    this.maxPoints = maxPoints;
    this.maxSubmissions = maxSubmissions;
    this.bestSubmissionId = bestSubmissionId;
  }

  /**
   * Construct an exercise from the given JSON object. The object must contain an integer value for
   * the key "id", a string value for the key "display_name", a string value for the key "html_url",
   * and integer values for the keys "max_points" and "max_submissions".
   *
   * @param jsonObject The JSON object from which the exercise is constructed.
   * @return An exercise instance.
   */
  @NotNull
  public static Exercise fromJsonObject(@NotNull JSONObject jsonObject,
                                        @NotNull Points points,
                                        @NotNull Map<Long, Tutorial> tutorials) {
    long id = jsonObject.getLong("id");

    String name = jsonObject.getString("display_name");
    String htmlUrl = jsonObject.getString("html_url");

    var bestSubmissionId = points.getBestSubmissionIds().get(id);
    int maxPoints = jsonObject.getInt("max_points");
    int maxSubmissions = jsonObject.getInt("max_submissions");

    var submissionInfo = SubmissionInfo.fromJsonObject(jsonObject);

    var tutorial = tutorials.get(id);
    var optionalBestSubmission = bestSubmissionId == null ? OptionalLong.empty()
        : OptionalLong.of(bestSubmissionId);
    if (tutorial == null) {
      return new Exercise(id, name, htmlUrl, submissionInfo, maxPoints, maxSubmissions, optionalBestSubmission);
    } else {
      return new TutorialExercise(
          id, name, htmlUrl, submissionInfo, maxPoints, maxSubmissions, optionalBestSubmission, tutorial);
    }
  }

  public long getId() {
    return id;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @Override
  public @NotNull String getHtmlUrl() {
    return htmlUrl;
  }

  public void addSubmissionResult(@NotNull SubmissionResult submissionResult) {
    submissionResults.add(submissionResult);
  }

  @NotNull
  public List<SubmissionResult> getSubmissionResults() {
    return Collections.unmodifiableList(submissionResults);
  }

  @NotNull
  public SubmissionInfo getSubmissionInfo() {
    return submissionInfo;
  }

  public int getUserPoints() {
    return getBestSubmission() != null ? getBestSubmission().getPoints() : 0;
  }

  public int getMaxPoints() {
    return maxPoints;
  }

  public int getMaxSubmissions() {
    return maxSubmissions;
  }

  /**
   * Returns the best submission of this exercise (if one exists).
   */
  @Nullable
  public SubmissionResult getBestSubmission() {
    synchronized (submissionResults) {
      return submissionResults
          .stream()
          .filter(submission -> OptionalLong.of(submission.getId()).equals(bestSubmissionId))
          .findFirst()
          .orElse(null);
    }
  }

  public boolean isSubmittable() {
    return submissionInfo.isSubmittable();
  }

  /**
   * Returns true if assignment is completed.
   *
   * @return True if userPoints are the same as maxPoints, otherwise False
   */
  public boolean isCompleted() {
    // Optional assignments are never completed, since they can be filtered separately
    // and we can't tell from the points whether the submission was correct or not
    return getUserPoints() == maxPoints && !isOptional();
  }

  public boolean isOptional() {
    return maxSubmissions == 0 && maxPoints == 0;
  }

  /**
   * Returns true if any of the submissions of this exercise has status WAITING.
   */
  public boolean isInGrading() {
    synchronized (submissionResults) {
      return submissionResults
          .stream()
          .anyMatch(submission -> submission.getStatus() == SubmissionResult.Status.WAITING);
    }
  }

  public boolean isLate() {
    return getBestSubmission() != null && getBestSubmission().getLatePenalty() != 0.0;
  }

  public boolean isDummy() {
    return false;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Exercise && ((Exercise) obj).getId() == getId();
  }

  @Override
  public String toString() {
    return "Exercise{"
        + "id=" + id
        + ", name='" + name + '\''
        + '}';
  }
}
