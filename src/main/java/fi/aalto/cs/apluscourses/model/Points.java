package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class Points {

  @NotNull
  private final Map<Long, List<Long>> submissions;

  @NotNull
  private final Map<Long, Integer> exercisePoints;

  @NotNull
  private final Map<Long, Integer> submissionPoints;

  @NotNull
  private final Map<Long, Long> bestSubmissions;

  // TODO: remove
  @NotNull
  private Set<Long> submittableExercises;

  /**
   * Construct an instance with the given maps.
   *
   * @param submissions      A map of exercise IDs to a list of submission IDs for that exercise.
   *                         The first element of the list should be the ID of the oldest submission
   *                         and the last element should be the ID of the latest submission.
   * @param exercisePoints   A map of exercise IDs to the best points gotten from that exercise.
   * @param submissionPoints A map of submission IDs to the points of that submission.
   * @param bestSubmissions  A map of exercise IDs to the IDs of the best submission for each
   *                         exercise.
   */
  public Points(@NotNull Map<Long, List<Long>> submissions,
                @NotNull Map<Long, Integer> exercisePoints,
                @NotNull Map<Long, Integer> submissionPoints,
                @NotNull Map<Long, Long> bestSubmissions) {
    this.submissions = submissions;
    this.exercisePoints = exercisePoints;
    this.submissionPoints = submissionPoints;
    this.bestSubmissions = bestSubmissions;
    this.submittableExercises = Collections.emptySet();
  }

  @NotNull
  public Map<Long, List<Long>> getSubmissions() {
    return Collections.unmodifiableMap(submissions);
  }

  @NotNull
  public Map<Long, Integer> getExercisePoints() {
    return Collections.unmodifiableMap(exercisePoints);
  }

  @NotNull
  public Map<Long, Integer> getSubmissionPoints() {
    return Collections.unmodifiableMap(submissionPoints);
  }

  @NotNull
  public Map<Long, Long> getBestSubmissionIds() {
    return Collections.unmodifiableMap(bestSubmissions);
  }

  /**
   * DO NOT USE THIS, AS IT IS LIKELY TO BE REMOVED.
   */
  @Deprecated
  public boolean isSubmittable(long exerciseId) {
    return submittableExercises.contains(exerciseId);
  }

  /**
   * DO NOT USE THIS, AS IT IS LIKELY TO BE REMOVED.
   */
  @Deprecated
  public void setSubmittableExercises(@NotNull Set<Long> submittableExercises) {
    this.submittableExercises = submittableExercises;
  }

  /**
   * Constructs a {@link Points} instance from the given JSON object.
   *
   * @param jsonObject The JSON object from which the {@link Points} instance is constructed.
   */
  @NotNull
  public static Points fromJsonObject(@NotNull JSONObject jsonObject) {
    JSONArray modulesArray = jsonObject.getJSONArray("modules");
    Map<Long, List<Long>> submissions = new HashMap<>();
    Map<Long, Integer> exercisePoints = new HashMap<>();
    Map<Long, Integer> submissionPoints = new HashMap<>();
    Map<Long, Long> bestSubmissions = new HashMap<>();
    for (int i = 0; i < modulesArray.length(); ++i) {
      JSONObject module = modulesArray.getJSONObject(i);
      JSONArray exercisesArray = module.getJSONArray("exercises");
      for (int j = 0; j < exercisesArray.length(); ++j) {
        JSONObject exercise = exercisesArray.getJSONObject(j);
        long exerciseId = exercise.getLong("id");

        parseSubmissions(exercise, exerciseId, submissions, submissionPoints);

        var bestSubmissionId = parseSubmissionId(exercise.optString("best_submission"));
        if (bestSubmissionId != null) {
          bestSubmissions.put(exerciseId, bestSubmissionId);
        }

        Integer points = exercise.getInt("points");
        exercisePoints.put(exerciseId, points);
      }
    }
    return new Points(submissions, exercisePoints, submissionPoints, bestSubmissions);
  }

  /*
   * Parses the submissions (IDs and points) from the given JSON and adds them to the given maps.
   */
  private static void parseSubmissions(@NotNull JSONObject exerciseJson,
                                       long exerciseId,
                                       @NotNull Map<Long, List<Long>> submissions,
                                       @NotNull Map<Long, Integer> submissionPoints) {
    JSONArray submissionsArray = exerciseJson.getJSONArray("submissions_with_points");
    List<Long> submissionIds = new ArrayList<>(submissionsArray.length());
    for (int i = submissionsArray.length() - 1; i >= 0; --i) {
      JSONObject submission = submissionsArray.getJSONObject(i);
      long submissionId = submission.getLong("id");
      submissionIds.add(submissionId);
      submissionPoints.put(submissionId, submission.getInt("grade"));
    }
    submissions.put(exerciseId, submissionIds);
  }

  @Nullable
  private static Long parseSubmissionId(String submissionUrl) {
    if (submissionUrl.isEmpty()) {
      return null;
    }
    if (submissionUrl.endsWith("/")) {
      submissionUrl = submissionUrl.substring(0, submissionUrl.length() - 1);
    }
    try {
      return Long.parseLong(submissionUrl.substring(submissionUrl.lastIndexOf("/") + 1));
    } catch (Exception e) {
      return null;
    }
  }

}
