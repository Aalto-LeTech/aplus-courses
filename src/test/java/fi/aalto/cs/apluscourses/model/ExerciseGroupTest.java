package fi.aalto.cs.apluscourses.model;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseGroupTest {

  private static final String NAME_KEY = "display_name";
  private static final String ID_KEY = "id";
  private static final String HTML_KEY = "html_url";
  private static final String OPEN_KEY = "is_open";

  @Test
  public void testExerciseGroup() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise1 = new Exercise(123, "name1", "https://example.com", info, 0, 0, 0);
    Exercise exercise2 = new Exercise(456, "name2", "https://example.org", info, 0, 0, 0);

    ExerciseGroup group = new ExerciseGroup(22, "group", "https://example.fi", true);
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
    ExerciseGroup group = new ExerciseGroup(10, "", "", true);
    group.getExercises().add(null);
  }

  @Test
  public void testFromJsonObject() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 567)
        .put(NAME_KEY, "group name")
        .put(HTML_KEY, "http://example.com/w01")
        .put(OPEN_KEY, true);
    ExerciseGroup group = ExerciseGroup.fromJsonObject(json);

    Assert.assertEquals(567, group.getId());
    Assert.assertEquals("The exercise group has the same name as in the JSON object",
        "group name", group.getName());
    Assert.assertEquals("http://example.com/w01", group.getHtmlUrl());
    Assert.assertTrue(group.isOpen());
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingExercises() {
    JSONObject json = new JSONObject().put(NAME_KEY, "group test name");
    ExerciseGroup.fromJsonObject(json);
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 100);
    ExerciseGroup.fromJsonObject(json);
  }

  @Test
  public void testFromJsonArray() {
    JSONArray array = new JSONArray();
    for (int i = 0; i < 5; ++i) {
      JSONObject json = new JSONObject()
          .put(NAME_KEY, "group " + i)
          .put(HTML_KEY, "http://example.com/w01")
          .put(OPEN_KEY, true)
          .put(ID_KEY, i);
      array.put(json);
    }
    List<ExerciseGroup> exerciseGroups =
        ExerciseGroup.fromJsonArray(array);

    for (int i = 0; i < 5; ++i) {
      Assert.assertEquals("group " + i, exerciseGroups.get(i).getName());
      Assert.assertEquals(i, exerciseGroups.get(i).getId());
    }
  }

}
