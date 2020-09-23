package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class ExerciseTest {

  @Test
  public void testExercise() {
    Exercise exercise = new Exercise(987, "def", "http://localhost:4444", 13, 15, 10);

    assertEquals("The ID is the same as the one given to the constructor",
        987L, exercise.getId());
    assertEquals("The name is the same as the one given to the constructor",
        "def", exercise.getName());
    assertEquals("The HTML URL is the same as the one given to the constructor",
        "http://localhost:4444", exercise.getHtmlUrl());
    assertEquals("The user points are the same as those given to the constructor",
        13, exercise.getUserPoints());
    assertEquals("The maximum points are the same as those given to the constructor",
        15, exercise.getMaxPoints());
    assertEquals("The maximum submissions are the same as those given to the constructor",
        10, exercise.getMaxSubmissions());
  }

  @NotNull
  @Contract(" -> new")
  private static Points createTestPoints() {
    long exerciseId = 11L;
    List<Long> submissionIds = Arrays.asList(1L, 2L);
    Map<Long, List<Long>> submissions = Collections.singletonMap(exerciseId, submissionIds);
    Map<Long, Integer> exercisePoints = Collections.singletonMap(exerciseId, 10);
    Map<Long, Integer> submissionPoints = new HashMap<>();
    submissionPoints.put(1L, 33);
    submissionPoints.put(2L, 44);
    return new Points(submissions, exercisePoints, submissionPoints);
  }

  private static final Points TEST_POINTS = createTestPoints();
  private static final String ID_KEY = "id";
  private static final String NAME_KEY = "display_name";
  private static final String HTML_KEY = "html_url";
  private static final String MAX_POINTS_KEY = "max_points";
  private static final String MAX_SUBMISSIONS_KEY = "max_submissions";

  @Test
  public void testExerciseFromJsonObject() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 11L)
        .put(NAME_KEY, "Cool name")
        .put(HTML_KEY, "http://localhost:1000")
        .put(MAX_POINTS_KEY, 99)
        .put(MAX_SUBMISSIONS_KEY, 5)
        .put("additional key", "which shouldn't cause errors");

    Exercise exercise = Exercise.fromJsonObject(json, TEST_POINTS);

    assertEquals("The ID is the same as the one in the JSON object",
        11L, exercise.getId());
    assertEquals("The name is the same as the one in the JSON object",
        "Cool name", exercise.getName());
    assertEquals("The HTML URL is the same as the one in the JSON object",
        "http://localhost:1000", exercise.getHtmlUrl());
    assertEquals("The submission IDs are read from the points object",
        1L, exercise.getSubmissionResults().get(0).getId());
    assertEquals("The submission IDs are read from the points object",
        2L, exercise.getSubmissionResults().get(1).getId());
    assertEquals("The submission points are read from the points object",
        33, exercise.getSubmissionResults().get(0).getPoints());
    assertEquals("The submission points are read from the points object",
        44, exercise.getSubmissionResults().get(1).getPoints());
    assertEquals("The user points are read from the points object",
        10, exercise.getUserPoints());
    assertEquals("The max points is the same as the one in the JSON object",
        99, exercise.getMaxPoints());
    assertEquals("The max submissions is the same as the one in the JSON object",
        5, exercise.getMaxSubmissions());
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingId() {
    JSONObject json = new JSONObject()
        .put(NAME_KEY, "A name")
        .put(HTML_KEY, "https://example.com")
        .put(MAX_POINTS_KEY, 55)
        .put(MAX_SUBMISSIONS_KEY, 3);

    Exercise.fromJsonObject(json, TEST_POINTS);
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(HTML_KEY, "https://example.org")
        .put(MAX_POINTS_KEY, 44)
        .put(MAX_SUBMISSIONS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS);
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingMaxPoints() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(NAME_KEY, "another name")
        .put(HTML_KEY, "http://localhost:4567")
        .put(MAX_SUBMISSIONS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS);
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingMaxSubmissions() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(NAME_KEY, "yet another name")
        .put(HTML_KEY, "localhost:1234")
        .put(MAX_POINTS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS);
  }

  @Test
  public void testEquals() {
    Exercise exercise = new Exercise(7, "oneEx", "http://localhost:1111", 0, 0, 0);
    Exercise sameExercise = new Exercise(7, "twoEx", "http://localhost:2222", 2, 3, 4);
    Exercise otherExercise = new Exercise(4, "oneEx", "http://localhost:2222", 3, 2, 1);

    assertEquals(exercise, sameExercise);
    assertEquals(exercise.hashCode(), sameExercise.hashCode());

    assertNotEquals(exercise, otherExercise);
  }
}
