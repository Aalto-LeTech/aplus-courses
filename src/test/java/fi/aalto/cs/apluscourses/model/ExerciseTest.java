package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
    Exercise exercise = new Exercise(987, "def", "http://localhost:4444", 13, 15, 10, false);

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
    assertFalse("The exercise submittability depends on the constructor parameter",
        exercise.isSubmittable());
    assertFalse("The exercise is In Grading (default value)", exercise.isInGrading());
  }

  @NotNull
  @Contract(" -> new")
  private static Points createTestPoints() {
    long exerciseId = 11L;
    List<Long> submissionIds = List.of(1L, 2L);
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

    Exercise exercise = Exercise.fromJsonObject(json, TEST_POINTS, );

    assertEquals("The ID is the same as the one in the JSON object",
        11L, exercise.getId());
    assertEquals("The name is the same as the one in the JSON object",
        "Cool name", exercise.getName());
    assertEquals("The HTML URL is the same as the one in the JSON object",
        "http://localhost:1000", exercise.getHtmlUrl());
    assertEquals("The user points are read from the points object",
        10, exercise.getUserPoints());
    assertEquals("The max points is the same as the one in the JSON object",
        99, exercise.getMaxPoints());
    assertEquals("The max submissions is the same as the one in the JSON object",
        5, exercise.getMaxSubmissions());
    assertFalse("The exercise is In Grading (default value)", exercise.isInGrading());
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingId() {
    JSONObject json = new JSONObject()
        .put(NAME_KEY, "A name")
        .put(HTML_KEY, "https://example.com")
        .put(MAX_POINTS_KEY, 55)
        .put(MAX_SUBMISSIONS_KEY, 3);

    Exercise.fromJsonObject(json, TEST_POINTS, );
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(HTML_KEY, "https://example.org")
        .put(MAX_POINTS_KEY, 44)
        .put(MAX_SUBMISSIONS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS, );
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingMaxPoints() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(NAME_KEY, "another name")
        .put(HTML_KEY, "http://localhost:4567")
        .put(MAX_SUBMISSIONS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS, );
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingMaxSubmissions() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(NAME_KEY, "yet another name")
        .put(HTML_KEY, "localhost:1234")
        .put(MAX_POINTS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS, );
  }

  @Test
  public void testEquals() {
    Exercise exercise = new Exercise(7, "oneEx", "http://localhost:1111", 0, 0, 0, true);
    Exercise sameExercise = new Exercise(7, "twoEx", "http://localhost:2222", 2, 3, 4, false);
    Exercise otherExercise = new Exercise(4, "oneEx", "http://localhost:2222", 3, 2, 1, true);

    assertEquals(exercise, sameExercise);
    assertEquals(exercise.hashCode(), sameExercise.hashCode());

    assertNotEquals(exercise, otherExercise);
  }

  @Test
  public void testIsCompleted() {
    Exercise optionalNotSubmitted
        = new Exercise(1, "optionalNotSubmitted", "http://localhost:1111", 0, 0, 0, true);
    Exercise optionalSubmitted
        = new Exercise(2, "optionalSubmitted", "http://localhost:1111", 0, 0, 0, true);
    optionalSubmitted.addSubmissionResult(new SubmissionResult(
            1, 0, SubmissionResult.Status.GRADED, optionalSubmitted));

    assertFalse("Optional assignment with no submissions isn't completed",
        optionalNotSubmitted.isCompleted());
    assertFalse("Optional assignment with submissions isn't completed",
        optionalSubmitted.isCompleted());

    Exercise noSubmissions
        = new Exercise(3, "noSubmissions", "http://localhost:1111", 0, 5, 10, true);
    Exercise failed
        = new Exercise(4, "failed", "http://localhost:1111", 3, 5, 10, true);
    failed.addSubmissionResult(new SubmissionResult(
            1, 3, SubmissionResult.Status.GRADED, failed));

    Exercise completed = new Exercise(5, "completed", "http://localhost:1111", 5, 5, 10, true);
    completed.addSubmissionResult(new SubmissionResult(
            1, 5, SubmissionResult.Status.GRADED, completed));


    assertFalse("Assignment with no submissions isn't completed",
        noSubmissions.isCompleted());
    assertFalse("Assignment with partial user points isn't completed",
        failed.isCompleted());
    assertTrue("Assignment with full user points is completed",
        completed.isCompleted());
  }

  @Test
  public void testIsOptional() {
    Exercise optional = new Exercise(1, "optional", "http://localhost:1111", 0, 0, 0, true);
    assertTrue("Assignment is optional",
            optional.isOptional());

    Exercise notOptional = new Exercise(2, "notOptional", "http://localhost:1111", 0, 5, 10, true);
    assertFalse("Assignment isn't optional",
        notOptional.isOptional());
  }

  @Test
  public void testIsInGrading() {
    var exercise = new Exercise(1, "", "http://localhost:1", 0, 10, 10, true);
    exercise.addSubmissionResult(new SubmissionResult(
        0, 0, SubmissionResult.Status.UNOFFICIAL, exercise
    ));
    assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        1, 0, SubmissionResult.Status.GRADED, exercise
    ));
    assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        2, 0, SubmissionResult.Status.UNKNOWN, exercise
    ));
    assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        3, 0, SubmissionResult.Status.WAITING, exercise
    ));
    assertTrue(exercise.isInGrading());
  }

}
