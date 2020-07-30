package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class Points {

  @NotNull
  private final Map<Long, List<Long>> submissions;

  @NotNull
  private final Map<Long, Integer> points;

  public Points() {
    this(Collections.emptyMap(), Collections.emptyMap());
  }

  /**
   * Construct an instance with the given maps.
   * @param submissions A map of exercise IDs to a list of submissions IDs for that exercise.
   * @param points      A map of exercise IDs to the best points gotten from that exercise.
   */
  public Points(@NotNull Map<Long, List<Long>> submissions, @NotNull Map<Long, Integer> points) {
    this.submissions = submissions;
    this.points = points;
  }

  @NotNull
  public Map<Long, List<Long>> getSubmissions() {
    return Collections.unmodifiableMap(submissions);
  }

  @NotNull
  public Map<Long, Integer> getPoints() {
    return Collections.unmodifiableMap(points);
  }

  /**
   * Constructs a {@link Points} instance from the given JSON object. The object must contain an
   * array for the key "modules". Each element of the array should have an "exercises" array, where
   * each exercise has an id, an array for the key "submissions", and an integer value for the key
   * "points".
   * Constructs a {@link Points} from the given JSON object. The object must contain
   * an long value for the key "id", integer value for the key "points" and an array value for the
   * key "modules" (containing another array for the key "exercises" in its turn).
   *
   * @param jsonObject The JSON object from which the {@link Points} instance is constructed.
   */
  @NotNull
  public static Points fromJsonObject(@NotNull JSONObject jsonObject) {
    JSONArray modulesArray = jsonObject.getJSONArray("modules");
    Map<Long, List<Long>> submissions = new HashMap<>();
    Map<Long, Integer> bestPoints = new HashMap<>();
    for (int i = 0; i < modulesArray.length(); ++i) {
      JSONObject module = modulesArray.getJSONObject(i);
      JSONArray exercisesArray = module.getJSONArray("exercises");
      for (int j = 0; j < exercisesArray.length(); ++j) {
        JSONObject exercise = exercisesArray.getJSONObject(j);
        Long exerciseId = exercise.getLong("id");

        List<Long> submissionIds = getSubmissionIds(exercise);
        submissions.put(exerciseId, submissionIds);

        Integer points = exercise.getInt("points");
        bestPoints.put(exerciseId, points);
      }
    }
    return new Points(submissions, bestPoints);
  }

  @NotNull
  private static List<Long> getSubmissionIds(@NotNull JSONObject exercise) {
    JSONArray submissionsArray = exercise.getJSONArray("submissions");
    List<Long> submissionIds = new ArrayList<>(submissionsArray.length());
    for (int i = 0; i < submissionsArray.length(); ++i) {
      String submissionUrl = submissionsArray.getString(i);
      submissionIds.add(parseSubmissionId(submissionUrl));
    }
    return submissionIds;
  }

  private static long parseSubmissionId(@NotNull String submissionUrl) {
    return Long.parseLong(
        StringUtils.substringBetween(submissionUrl, "/submissions/", "/")
    );
  }

}
