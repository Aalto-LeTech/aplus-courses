package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import fi.aalto.cs.apluscourses.utils.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExerciseGroup implements Browsable {

  private final long id;
  @NotNull
  private final String name;
  @NotNull
  private final String htmlUrl;
  private final boolean isOpen;
  private final List<Long> exerciseOrder;
  @NotNull
  private final List<Exercise> exercises = Collections.synchronizedList(new ArrayList<>());

  /**
   * Construct an exercise group with the given name and exercises.
   */
  public ExerciseGroup(long id,
                       @NotNull String name,
                       @NotNull String htmlUrl,
                       boolean isOpen,
                       List<DummyExercise> dummyExercises,
                       @NotNull List<Long> exerciseOrder) {
    this.id = id;
    this.name = name;
    this.htmlUrl = htmlUrl;
    this.isOpen = isOpen;
    this.exerciseOrder = exerciseOrder;
    this.exercises.addAll(dummyExercises);
    sort();
  }

  private void sort() {
    this.exercises.sort(Comparator.comparing(exercise -> exerciseOrder.indexOf(exercise.getId())));
  }

  /**
   * Construct an exercise group from the given JSON object. The JSON object must contain a long
   * with the key "id", a string with the key "display_name", and an array with the key "exercises".
   * Each of the JSON objects in the exercise array is given to {@link Exercise#fromJsonObject}.
   *
   * @param jsonObject The JSON object from which the exercise group is constructed.
   */
  @NotNull
  public static ExerciseGroup fromJsonObject(@NotNull JSONObject jsonObject,
                                             @NotNull Map<Long, List<Long>> exerciseOrder,
                                             @NotNull String languageCode) {
    long id = jsonObject.getLong("id");
    String name = APlusLocalizationUtil.getLocalizedName(jsonObject.getString("display_name"), languageCode);
    String htmlUrl = jsonObject.getString("html_url");
    boolean isOpen = jsonObject.getBoolean("is_open");
    JSONArray exercisesArray = jsonObject.getJSONArray("exercises");
    DummyExercise[] dummyExercises = JsonUtil.parseArray(exercisesArray,
        JSONArray::getJSONObject,
        (obj) -> DummyExercise.fromJsonObject(obj, languageCode),
        DummyExercise[]::new);
    return new ExerciseGroup(id, name, htmlUrl, isOpen, Arrays.stream(dummyExercises).collect(Collectors.toList()),
        exerciseOrder.get(id));
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

  /**
   * Adds an exercise or replaces an existing one.
   */
  public void addExercise(@NotNull Exercise exercise) {
    var oldExercise = exercises.stream().filter(oldEx -> oldEx.equals(exercise)).findFirst();
    oldExercise.ifPresent(exercises::remove);
    var index = exerciseOrder.indexOf(exercise.getId());
    if (index == -1) {
      exercises.add(exercise);
    } else if (index > exercises.size() - 1) {
      exercises.add(exercise);
      sort();
    } else {
      exercises.add(index, exercise);
    }
  }
}
