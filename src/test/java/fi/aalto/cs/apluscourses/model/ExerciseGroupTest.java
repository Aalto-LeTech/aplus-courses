package fi.aalto.cs.apluscourses.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExerciseGroupTest {

  private static final String NAME_KEY = "display_name";
  private static final String EXERCISES_KEY = "exercises";
  private static final String ID_KEY = "id";
  private static final String HTML_KEY = "html_url";
  private static final String OPEN_KEY = "is_open";
  private static final String MAX_SUBMISSIONS_KEY = "max_submissions";
  private static final String MAX_POINTS_KEY = "max_points";

  @Test
  void testExerciseGroup() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise1 = new Exercise(123, "name1", "https://example.com", info, 0, 0,
        OptionalLong.empty(), null, false);
    Exercise exercise2 = new Exercise(456, "name2", "https://example.org", info, 0, 0,
        OptionalLong.empty(), null, false);

    ExerciseGroup group = new ExerciseGroup(22, "group", "https://example.fi", true, List.of(), List.of());
    group.addExercise(exercise1);
    group.addExercise(exercise2);

    Assertions.assertEquals(22, group.getId());
    Assertions.assertEquals("https://example.fi", group.getHtmlUrl(),
        "The url is the same as the one given to the constructor");
    Assertions.assertTrue(group.isOpen(), "The open value is the same as the one given to the constructor");
    Assertions.assertEquals("group", group.getName(), "The name is the same as the one given to the constructor");
    Assertions.assertEquals("name1", group.getExercises().get(0).getName(),
        "The exercises are the same as those added to the group");
    Assertions.assertEquals("name2", group.getExercises().get(1).getName(),
        "The exercises are the same as those added to the group");
  }

  @Test
  void testGetExercisesReturnsUnmodifiableList() {
    ExerciseGroup group = new ExerciseGroup(10, "", "", true, List.of(), List.of());
    assertThrows(UnsupportedOperationException.class, () ->
        group.getExercises().add(null));
  }

  @Test
  void testFromJsonObject() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 567)
        .put(NAME_KEY, "group name")
        .put(HTML_KEY, "http://example.com/w01")
        .put(OPEN_KEY, true)
        .put(EXERCISES_KEY, new JSONArray()
            .put(new JSONObject()
                .put(ID_KEY, 567)
                .put(NAME_KEY, "exercise name")
                .put(HTML_KEY, "http://localhost:7000")
                .put(MAX_POINTS_KEY, 50)
                .put(MAX_SUBMISSIONS_KEY, 10)));
    ExerciseGroup group = ExerciseGroup.fromJsonObject(json, Map.of(567L, List.of()));

    Assertions.assertEquals(567, group.getId());
    Assertions.assertEquals("group name", group.getName(),
        "The exercise group has the same name as in the JSON object");
    Assertions.assertEquals("http://example.com/w01", group.getHtmlUrl());
    Assertions.assertTrue(group.isOpen());
    Assertions.assertEquals("exercise name", group.getExercises().get(0).getName(),
        "The exercise group has the same exercises as in the JSON object");
  }

  @Test
  void testFromJsonObjectMissingExercises() {
    JSONObject json = new JSONObject().put(NAME_KEY, "group test name");
    assertThrows(JSONException.class, () ->
        ExerciseGroup.fromJsonObject(json, new HashMap<>()));
  }

  @Test
  void testFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 100);
    assertThrows(JSONException.class, () ->
        ExerciseGroup.fromJsonObject(json, new HashMap<>()));
  }

}
