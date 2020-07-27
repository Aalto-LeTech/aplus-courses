package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExerciseGroup {
  @NotNull
  private String name;

  @NotNull
  private Map<Long, Exercise> exercises;

  /**
   * Construct an exercise group with the given name and exercises.
   *
   * @param name      The name of the exercise group.
   * @param exercises The exercises of this exercise group.
   */
  public ExerciseGroup(@NotNull String name, @NotNull List<Exercise> exercises) {
    this.name = name;
    this.exercises = exercises
        .stream()
        .collect(Collectors.toMap(Exercise::getId, Function.identity()));
  }

  /**
   * Construct an exercise group from the given JSON object. The JSON object must contain a string
   * with the key "display_name" and an array with the key "exercises". Each of the JSON objects
   * in the exercise array is given to {@link Exercise#fromJsonObject}.
   *
   * @param jsonObject The JSON object from which the exercise group is constructed.
   */
  @NotNull
  public static ExerciseGroup fromJsonObject(@NotNull JSONObject jsonObject,
                                             @NotNull Points points) {
    String name = jsonObject.getString("display_name");
    JSONArray exercisesArray = jsonObject.getJSONArray("exercises");
    List<Exercise> exercises = new ArrayList<>(exercisesArray.length());
    for (int i = 0; i < exercisesArray.length(); ++i) {
      JSONObject exerciseObject = exercisesArray.getJSONObject(i);
      exercises.add(Exercise.fromJsonObject(exerciseObject, points));
    }
    return new ExerciseGroup(name, exercises);
  }

  /**
   * Return the list of exercise groups contained in the given JSON array. Each element in the array
   * should be a JSON object that works with {@link ExerciseGroup#fromJsonObject}.
   */
  @NotNull
  public static List<ExerciseGroup> fromJsonArray(@NotNull JSONArray jsonArray,
                                                  @NotNull Points points) {
    List<ExerciseGroup> exerciseGroups = new ArrayList<>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); ++i) {
      exerciseGroups.add(fromJsonObject(jsonArray.getJSONObject(i), points));
    }
    return exerciseGroups;
  }

  public String getName() {
    return name;
  }

  /**
   * Returns a map that contains the exercises of this exercise group. The keys are IDs of exercises
   * and the values are the exercises corresponding to the IDs.
   */
  public Map<Long, Exercise> getExercises() {
    return Collections.unmodifiableMap(exercises);
  }
}
