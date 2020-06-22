package fi.aalto.cs.apluscourses.model;

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

  @Test
  public void testExerciseGroup() {
    Exercise exercise1 = new Exercise(123, "name1");
    Exercise exercise2 = new Exercise(456, "name2");

    ExerciseGroup group = new ExerciseGroup("group", Arrays.asList(exercise1, exercise2));

    Assert.assertEquals("The name is the same as the one given to the constructor",
        "group", group.getName());
    Assert.assertEquals("The exercises are the same as those given to the constructor",
        "name1", group.getExercises().get(123L).getName());
    Assert.assertEquals("The exercises are the same as those given to the constructor",
        "name2", group.getExercises().get(456L).getName());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetExercisesReturnsUnmodifiableMap() {
    ExerciseGroup group = new ExerciseGroup("", new ArrayList<>());
    group.getExercises().put(999L, new Exercise(999, "test name"));
  }

  @Test
  public void testFromJsonObject() {
    JSONObject json = new JSONObject()
        .put(NAME_KEY, "group name")
        .put(EXERCISES_KEY, new JSONArray()
            .put(new JSONObject()
                .put("id", 567)
                .put(NAME_KEY, "exercise name")));
    ExerciseGroup group = ExerciseGroup.fromJsonObject(json);

    Assert.assertEquals("The exercise group has the same name as in the JSON object",
        "group name", group.getName());
    Assert.assertEquals("The exercise group has the same exercises as in the JSON object",
        "exercise name", group.getExercises().get(567L).getName());
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingExercises() {
    JSONObject json = new JSONObject().put(NAME_KEY, "group test name");
    ExerciseGroup.fromJsonObject(json);
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(EXERCISES_KEY, new JSONArray()
            .put(new JSONObject()
                .put("id", 0)
                .put(NAME_KEY, "e")));
    ExerciseGroup.fromJsonObject(json);
  }

  @Test
  public void testFromJsonArray() {
    JSONArray array = new JSONArray();
    for (int i = 0; i < 5; ++i) {
      JSONObject json = new JSONObject()
          .put(NAME_KEY, "group " + i)
          .put(EXERCISES_KEY, new JSONArray()
              .put(new JSONObject()
                  .put("id", i)
                  .put(NAME_KEY, "exericse in group " + i)));
      array.put(json);
    }
    List<ExerciseGroup> exerciseGroups = ExerciseGroup.fromJsonArray(array);

    for (int i = 0; i < 5; ++i) {
      Assert.assertEquals("group " + i, exerciseGroups.get(i).getName());
    }
  }

}
