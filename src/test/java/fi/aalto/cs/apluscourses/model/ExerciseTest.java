package fi.aalto.cs.apluscourses.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseTest {

  @Test
  public void testExercise() {
    Exercise exercise = new Exercise(987, "def");

    Assert.assertEquals("The ID is the same as the one given to the constructor",
        987L, exercise.getId());
    Assert.assertEquals("The name is the same as the one given to the constructor",
        "def", exercise.getName());
  }

  private static final String ID_KEY = "id";
  private static final String NAME_KEY = "display_name";

  @Test
  public void testExerciseFromJsonObject() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 123)
        .put(NAME_KEY, "Cool name")
        .put("additional key", "which shouldn't cause errors");

    Exercise exercise = Exercise.fromJsonObject(json);

    Assert.assertEquals("The ID is the same as the one in the JSON object",
        123L, exercise.getId());
    Assert.assertEquals("The name is the same as the one in the JSON object",
        "Cool name", exercise.getName());
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingId() {
    JSONObject json = new JSONObject()
        .put(NAME_KEY, "A name");

    Exercise.fromJsonObject(json);
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357);

    Exercise.fromJsonObject(json);
  }

}
