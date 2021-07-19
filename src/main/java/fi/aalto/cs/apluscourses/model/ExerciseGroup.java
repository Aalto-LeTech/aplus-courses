package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExerciseGroup implements Browsable {

  private final long id;
  @NotNull
  private final String name;
  @NotNull
  private final String htmlUrl;
  private final boolean isOpen;
  private final List<DummyExercise> dummyExercises;
  @NotNull
  private final List<Exercise> exercises = Collections.synchronizedList(new ArrayList<>());

  /**
   * Construct an exercise group with the given name and exercises.
   */
  public ExerciseGroup(long id,
                       @NotNull String name,
                       @NotNull String htmlUrl,
                       boolean isOpen,
                       List<DummyExercise> dummyExercises) {
    this.id = id;
    this.name = name;
    this.htmlUrl = htmlUrl;
    this.isOpen = isOpen;
    this.dummyExercises = dummyExercises;
    exercises.addAll(dummyExercises);
  }

  /**
   * Construct an exercise group from the given JSON object. The JSON object must contain a long
   * with the key "id", a string with the key "display_name", and an array with the key "exercises".
   * Each of the JSON objects in the exercise array is given to {@link Exercise#fromJsonObject}.
   *
   * @param jsonObject The JSON object from which the exercise group is constructed.
   */
  @NotNull
  public static ExerciseGroup fromJsonObject(@NotNull JSONObject jsonObject) {
    long id = jsonObject.getLong("id");
    String name = jsonObject.getString("display_name");
    String htmlUrl = jsonObject.getString("html_url");
    boolean isOpen = jsonObject.getBoolean("is_open");
    JSONArray exercisesArray = jsonObject.getJSONArray("exercises");
    DummyExercise[] dummyExercises = JsonUtil.parseArray(exercisesArray,
        JSONArray::getJSONObject,
        DummyExercise::fromJsonObject,
        DummyExercise[]::new);
    return new ExerciseGroup(id, name, htmlUrl, isOpen, Arrays.stream(dummyExercises).collect(Collectors.toList()));
  }

  /**
   * Return the list of exercise groups contained in the given JSON array. Each element in the array
   * should be a JSON object that works with {@link ExerciseGroup#fromJsonObject}.
   */
  @NotNull
  public static List<ExerciseGroup> fromJsonArray(@NotNull JSONArray jsonArray) {
    List<ExerciseGroup> exerciseGroups = new ArrayList<>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); ++i) {
      exerciseGroups.add(fromJsonObject(jsonArray.getJSONObject(i)));
    }
    return exerciseGroups;
  }

  public long getId() {
    return id;
  }

  public @NotNull String getName() {
    return name;
  }

  @Override
  public @NotNull String getHtmlUrl() {
    return htmlUrl;
  }

  public boolean isOpen() {
    return isOpen;
  }

  public List<Exercise> getExercises() {
    return Collections.unmodifiableList(exercises);
  }

  @Nullable
  public Exercise getExerciseById(long id) {
    return exercises.stream().filter(ex -> ex.getId() == id).findFirst().orElse(null);
  }

  /**
   * Adds an exercise or replaces an existing one.
   */
  public void addExercise(@NotNull Exercise exercise) {
    var oldExercise = exercises.stream().filter(oldEx -> oldEx.equals(exercise)).findFirst();
    if (oldExercise.isPresent()) {
      exercises.remove(oldExercise.get());
      if (oldExercise.get() instanceof DummyExercise) {
        dummyExercises.remove(oldExercise.get());
      }
    }
    exercises.add(exercise);
  }

}
