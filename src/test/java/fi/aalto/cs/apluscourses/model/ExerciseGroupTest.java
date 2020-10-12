package fi.aalto.cs.apluscourses.model;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
  private static final String MAX_SUBMISSIONS_KEY = "max_submissions";
  private static final String MAX_POINTS_KEY = "max_points";

  @Test
  public void testExerciseGroup() {
    Exercise exercise1 = new Exercise(123, "name1", "https://example.com", 0, 0, 0);
    Exercise exercise2 = new Exercise(456, "name2", "https://example.org", 0, 0, 0);

    ExerciseGroup group = new ExerciseGroup(22, "group", Arrays.asList(exercise1, exercise2));

    Assert.assertEquals(22, group.getId());
    Assert.assertEquals("The name is the same as the one given to the constructor",
        "group", group.getName());
    Assert.assertEquals("The exercises are the same as those given to the constructor",
        "name1", group.getExercises().get(123L).getName());
    Assert.assertEquals("The exercises are the same as those given to the constructor",
        "name2", group.getExercises().get(456L).getName());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetExercisesReturnsUnmodifiableMap() {
    ExerciseGroup group = new ExerciseGroup(10, "", new ArrayList<>());
    group.getExercises().put(999L, null);
  }

  @Test
  public void testFromJsonObject() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 567)
        .put(NAME_KEY, "group name")
        .put(EXERCISES_KEY, new JSONArray()
            .put(new JSONObject()
                .put(ID_KEY, 567)
                .put(NAME_KEY, "exercise name")
                .put(HTML_KEY, "http://localhost:7000")
                .put(MAX_POINTS_KEY, 50)
                .put(MAX_SUBMISSIONS_KEY, 10)));
    ExerciseGroup group = ExerciseGroup.fromJsonObject(json, mock(Points.class));

    Assert.assertEquals(567, group.getId());
    Assert.assertEquals("The exercise group has the same name as in the JSON object",
        "group name", group.getName());
    Assert.assertEquals("The exercise group has the same exercises as in the JSON object",
        "exercise name", group.getExercises().get(567L).getName());
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingExercises() {
    JSONObject json = new JSONObject().put(NAME_KEY, "group test name");
    ExerciseGroup.fromJsonObject(json, mock(Points.class));
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 100)
        .put(EXERCISES_KEY, new JSONArray()
            .put(new JSONObject()
                .put(ID_KEY, 0)
                .put(NAME_KEY, "e")
                .put(HTML_KEY, "http://localhost:3000")
                .put(MAX_POINTS_KEY, 45)
                .put(MAX_SUBMISSIONS_KEY, 9)));
    ExerciseGroup.fromJsonObject(json, mock(Points.class));
  }

  @Test
  public void testFromJsonArray() {
    JSONArray array = new JSONArray();
    for (int i = 0; i < 5; ++i) {
      JSONObject json = new JSONObject()
          .put(NAME_KEY, "group " + i)
          .put(ID_KEY, i)
          .put(EXERCISES_KEY, new JSONArray()
              .put(new JSONObject()
                  .put(ID_KEY, i)
                  .put(NAME_KEY, "exercise in group " + i)
                  .put(HTML_KEY, "http://localhost:4000")
                  .put(MAX_POINTS_KEY, 30)
                  .put(MAX_SUBMISSIONS_KEY, 8)));
      array.put(json);
    }
    List<ExerciseGroup> exerciseGroups = ExerciseGroup.fromJsonArray(array, mock(Points.class));

    for (int i = 0; i < 5; ++i) {
      Assert.assertEquals("group " + i, exerciseGroups.get(i).getName());
      Assert.assertEquals(i, exerciseGroups.get(i).getId());
    }
  }

}
