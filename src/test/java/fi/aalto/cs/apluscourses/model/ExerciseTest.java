package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class ExerciseTest {

  @Test
  public void testExercise() {
    Exercise exercise = new Exercise(987, "def");

    assertEquals("The ID is the same as the one given to the constructor",
        987L, exercise.getId());
    assertEquals("The name is the same as the one given to the constructor",
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

    assertEquals("The ID is the same as the one in the JSON object",
        123L, exercise.getId());
    assertEquals("The name is the same as the one in the JSON object",
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

  @Test
  public void testEquals() {
    Exercise exercise = new Exercise(7, "oneex");
    Exercise sameExercise = new Exercise(7, "twoex");
    Exercise otherExercise = new Exercise(4, "oneex");

    assertEquals(exercise, sameExercise);
    assertEquals(exercise.hashCode(), sameExercise.hashCode());

    assertNotEquals(exercise, otherExercise);
  }

}
