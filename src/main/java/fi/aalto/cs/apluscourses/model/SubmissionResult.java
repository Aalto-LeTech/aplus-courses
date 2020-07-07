package fi.aalto.cs.apluscourses.model;

import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class SubmissionResult {

  private int exerciseId;
  private List<Integer> submissionIds;
  private int submissionsCount;
  //  private int maxSubmissionsCount;
  private int pointsToPass;
  private int maxPoints;
  private int points;

  /**
   * A result of a single submission.
   *
   * @param exerciseId an id of the exercise.
   * @param submissionIds an {@link List} of submission ids.
   * @param submissionsCount an amount of submissions made.
   * @param pointsToPass a required amount of points to pass the exercise.
   * @param maxPoints a possible maximum amount of points for the exercise.
   * @param totalPoints a total amount of points achieved for the exercise.
   */
  public SubmissionResult(int exerciseId, List<Integer> submissionIds, int submissionsCount,
      int pointsToPass, int maxPoints, int points) {
    this.exerciseId = exerciseId;
    this.submissionIds = submissionIds;
    this.submissionsCount = submissionsCount;
    this.pointsToPass = pointsToPass;
    this.maxPoints = maxPoints;
    this.points = points;
  }

  /**
   * Constructs {@link SubmissionResult} from the given JSON object. The object must contain an long value for the
   * key "id", integer value for the key "points" and an array value for the key "modules"
   * (containing another array for the key "exercises" in its turn).
   *
   * @param jsonObject The JSON object from which the dashboard is constructed.
   * @return a SubmissionsDashboard instance.
   */
  @NotNull
  public static SubmissionResult fromJsonObject(@NotNull JSONObject jsonObject) {
    int exerciseId = jsonObject.getInt("id");
    List<Integer> submissionIds = getSubmissionIdsFromJsonArray(jsonObject.getJSONArray("submissions"));
    int submissionsCount = jsonObject.getInt("submission_count");
    int pointsToPass = jsonObject.getInt("points_to_pass");
    int points = jsonObject.getInt("points");
    int maxPoints = jsonObject.getInt("max_points");

    return new SubmissionResult(exerciseId, submissionIds, submissionsCount, pointsToPass,
        maxPoints, points);
  }

  /**
   * Extract the list of submission ids from the {@link JSONArray} provided
   *
   * @param jsonArray a {@link JSONArray} to extract data from
   * @return a {@link List} of integers representing submission ids
   */
  @NotNull
  public static List<Integer> getSubmissionIdsFromJsonArray(@NotNull JSONArray jsonArray) {
    return jsonArray
        .toList()
        .stream()
        .map(url -> (String) url)
        .map(url -> substringBetween(url, "/api/v2/submissions/", "/"))
        .map(Integer::parseInt)
        .collect(Collectors.toList());
  }

  public int getExerciseId() {
    return exerciseId;
  }

  public List<Integer> getSubmissionIds() {
    return submissionIds;
  }

  public int getSubmissionsCount() {
    return submissionsCount;
  }

  public int getPointsToPass() {
    return pointsToPass;
  }

  public int getMaxPoints() {
    return maxPoints;
  }

  public int getPoints() {
    return points;
  }
}