package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ExerciseGroup {
  @NotNull
  private String name;

  @NotNull
  private List<Exercise> exercises;

  public ExerciseGroup(@NotNull String name, @NotNull List<Exercise> exercises) {
    this.name = name;
    this.exercises = exercises;
  }

  /**
   * Construct an exercise group from the given JSON object. The JSON object must contain a string
   * with the key "display_name" and an array with the key "exercises". Each of the JSON objects
   * in the exercise array is given to {@link Exercise#fromJsonObject}.
   *
   * @param jsonObject The JSON object from which the exercise group is constructed.
   */
  @NotNull
  public static ExerciseGroup fromJsonObject(@NotNull JSONObject jsonObject) {
    String name = jsonObject.getString("display_name");
    JSONArray exercisesArray = jsonObject.getJSONArray("exercises");
    List<Exercise> exercises = new ArrayList<>(exercisesArray.length());
    for (int i = 0; i < exercisesArray.length(); ++i) {
      JSONObject exerciseObject = exercisesArray.getJSONObject(i);
      exercises.add(Exercise.fromJsonObject(exerciseObject));
    }
    return new ExerciseGroup(name, exercises);
  }

  /**
   * Return the list of exercise groups contained in the given JSON array. Each element in the array
   * should be a JSON object that works with {@link ExerciseGroup#fromJsonObject(JSONObject)}.
   */
  @NotNull
  public static List<ExerciseGroup> fromJsonArray(@NotNull JSONArray jsonArray) {
    List<ExerciseGroup> exerciseGroups = new ArrayList<>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); ++i) {
      exerciseGroups.add(fromJsonObject(jsonArray.getJSONObject(i)));
    }
    return exerciseGroups;
  }

  /**
   * Get all of the exercise groups in for the given course by making a request to the A+ API.
   * @throws IOException TODO
   */
  @NotNull
  public static List<ExerciseGroup> getCourseExerciseGroups(
      @NotNull Course course, @NotNull APlusAuthentication authentication) throws IOException {
    URL url = new URL(String.format("%1$s/courses/%2$s/exercises/",
        PluginSettings.A_PLUS_API_BASE_URL, course.getId()));
    InputStream inputStream = CoursesClient.fetch(url, authentication::addToRequest);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    JSONArray results = response.getJSONArray("results");
    return fromJsonArray(results);
  }

  public String getName() {
    return name;
  }

  public List<Exercise> getExercises() {
    return Collections.unmodifiableList(exercises);
  }
}
