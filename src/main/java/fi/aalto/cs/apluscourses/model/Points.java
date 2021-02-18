package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class Points {

  @NotNull
  private final Map<Long, List<Long>> submissions;

  @NotNull
  private final Map<Long, Integer> exercisePoints;

  @NotNull
  private final Map<Long, Integer> submissionPoints;

  // TODO: remove
  @NotNull
  private Set<Long> submittableExercises;

  /**
   * Construct an instance with the given maps.
   * @param submissions      A map of exercise IDs to a list of submission IDs for that exercise.
   *                         The first element of the list should be the ID of the oldest submission
   *                         and the last element should be the ID of the latest submission.
   * @param exercisePoints   A map of exercise IDs to the best points gotten from that exercise.
   * @param submissionPoints A map of submission IDs to the points of that submission.
   */
  public Points(@NotNull Map<Long, List<Long>> submissions,
                @NotNull Map<Long, Integer> exercisePoints,
                @NotNull Map<Long, Integer> submissionPoints) {
    this.submissions = submissions;
    this.exercisePoints = exercisePoints;
    this.submissionPoints = submissionPoints;
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
    for (int i = 0; i < modulesArray.length(); ++i) {
      JSONObject module = modulesArray.getJSONObject(i);
      JSONArray exercisesArray = module.getJSONArray("exercises");
      for (int j = 0; j < exercisesArray.length(); ++j) {
        JSONObject exercise = exercisesArray.getJSONObject(j);
        long exerciseId = exercise.getLong("id");

        parseSubmissions(exercise, exerciseId, submissions, submissionPoints);

        Integer points = exercise.getInt("points");
        exercisePoints.put(exerciseId, points);
      }
    }
    return new Points(submissions, exercisePoints, submissionPoints);
  }

  /*
   * Parses the submissions (IDs and points) from the given JSON and adds them to the given maps.
   */
  @NotNull
  private static void parseSubmissions(@NotNull JSONObject exerciseJson,
                                       @NotNull long exerciseId,
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

}
