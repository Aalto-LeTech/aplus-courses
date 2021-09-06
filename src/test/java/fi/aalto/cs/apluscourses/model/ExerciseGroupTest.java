package fi.aalto.cs.apluscourses.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalLong;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseGroupTest {

  private static final String NAME_KEY = "display_name";
  private static final String EXERCISES_KEY = "exercises";
  private static final String ID_KEY = "id";
  private static final String HTML_KEY = "html_url";
  private static final String OPEN_KEY = "is_open";
  private static final String MAX_SUBMISSIONS_KEY = "max_submissions";
  private static final String MAX_POINTS_KEY = "max_points";

  @Test
  public void testExerciseGroup() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise1 = new Exercise(123, "name1", "https://example.com", info, 0, 0, OptionalLong.empty());
    Exercise exercise2 = new Exercise(456, "name2", "https://example.org", info, 0, 0, OptionalLong.empty());

    ExerciseGroup group = new ExerciseGroup(22, "group", "https://example.fi", true, List.of(), List.of());
    group.addExercise(exercise1);
    group.addExercise(exercise2);

    Assert.assertEquals(22, group.getId());
    Assert.assertEquals("The url is the same as the one given to the constructor",
        "https://example.fi", group.getHtmlUrl());
    Assert.assertTrue("The open value is the same as the one given to the constructor",
        group.isOpen());
    Assert.assertEquals("The name is the same as the one given to the constructor",
        "group", group.getName());
    Assert.assertEquals("The exercises are the same as those added to the group",
        "name1", group.getExercises().get(0).getName());
    Assert.assertEquals("The exercises are the same as those added to the group",
        "name2", group.getExercises().get(1).getName());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetExercisesReturnsUnmodifiableList() {
    ExerciseGroup group = new ExerciseGroup(10, "", "", true, List.of(), List.of());
    group.getExercises().add(null);
  }

  @Test
  public void testFromJsonObject() {
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
    ExerciseGroup group = ExerciseGroup.fromJsonObject(json, new HashMap<>());

    Assert.assertEquals(567, group.getId());
    Assert.assertEquals("The exercise group has the same name as in the JSON object",
        "group name", group.getName());
    Assert.assertEquals("http://example.com/w01", group.getHtmlUrl());
    Assert.assertTrue(group.isOpen());
    Assert.assertEquals("The exercise group has the same exercises as in the JSON object",
        "exercise name", group.getExercises().get(0).getName());
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingExercises() {
    JSONObject json = new JSONObject().put(NAME_KEY, "group test name");
    ExerciseGroup.fromJsonObject(json, new HashMap<>());
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 100);
    ExerciseGroup.fromJsonObject(json, new HashMap<>());
  }

}
