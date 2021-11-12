package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class ExerciseTest {

  @Test
  public void testExercise() {
    var info = new SubmissionInfo(Map.of("de", List.of(new SubmittableFile("f1", "a"))));
    Exercise exercise = new Exercise(987, "def", "http://localhost:4444", info, 15, 10, OptionalLong.empty(), null);

    assertEquals("The ID is the same as the one given to the constructor",
        987L, exercise.getId());
    assertEquals("The name is the same as the one given to the constructor",
        "def", exercise.getName());
    assertEquals("The HTML URL is the same as the one given to the constructor",
        "http://localhost:4444", exercise.getHtmlUrl());
    assertEquals("The maximum points are the same as those given to the constructor",
        15, exercise.getMaxPoints());
    assertEquals("The maximum submissions are the same as those given to the constructor",
        10, exercise.getMaxSubmissions());
    assertEquals("The difficulty should be converted from null to an empty string",
        "", exercise.getDifficulty());
    assertTrue("The exercise submittability depends on the submission info parameter",
        exercise.isSubmittable());
    assertNull("The submission ID is the one given to the constructor",
        exercise.getBestSubmission());
    assertFalse("The exercise is In Grading (default value)", exercise.isInGrading());
  }

  @NotNull
  @Contract(" -> new")
  private static Points createTestPoints() {
    long exerciseId = 11L;
    List<Long> submissionIds = List.of(1L, 2L);
    var submissions = Collections.singletonMap(11L, submissionIds);
    var bestSubmissions = Collections.singletonMap(exerciseId, 2L);
    return new Points(Collections.emptyMap(), submissions, bestSubmissions);
  }

  private static final Points TEST_POINTS = createTestPoints();
  private static final String ID_KEY = "id";
  private static final String NAME_KEY = "display_name";
  private static final String HTML_KEY = "html_url";
  private static final String MAX_POINTS_KEY = "max_points";
  private static final String MAX_SUBMISSIONS_KEY = "max_submissions";
  private static final String DIFFICULTY_KEY = "difficulty";

  @Test
  public void testExerciseFromJsonObject() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 11L)
        .put(NAME_KEY, "Cool name")
        .put(HTML_KEY, "http://localhost:1000")
        .put(MAX_POINTS_KEY, 99)
        .put(MAX_SUBMISSIONS_KEY, 5)
        .put(DIFFICULTY_KEY, "ha")
        .put("additional key", "which shouldn't cause errors");

    Exercise exercise = Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptyMap());
    exercise.addSubmissionResult(
        new SubmissionResult(2, 0, 0.0, SubmissionResult.Status.GRADED, exercise));

    assertEquals("The ID is the same as the one in the JSON object",
        11L, exercise.getId());
    assertEquals("The name is the same as the one in the JSON object",
        "Cool name", exercise.getName());
    assertEquals("The HTML URL is the same as the one in the JSON object",
        "http://localhost:1000", exercise.getHtmlUrl());
    assertEquals("The best submission is read from the points object",
        2L, exercise.getBestSubmission().getId());
    assertEquals("The max points is the same as the one in the JSON object",
        99, exercise.getMaxPoints());
    assertEquals("The max submissions is the same as the one in the JSON object",
        5, exercise.getMaxSubmissions());
    assertEquals("The difficulty is the same as the one in the JSON object",
        "ha", exercise.getDifficulty());
    assertFalse("The missing exercise_info makes the exercise unsubmittable",
        exercise.isSubmittable());
    assertFalse("The exercise is In Grading (default value)", exercise.isInGrading());
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingId() {
    JSONObject json = new JSONObject()
        .put(NAME_KEY, "A name")
        .put(HTML_KEY, "https://example.com")
        .put(MAX_POINTS_KEY, 55)
        .put(MAX_SUBMISSIONS_KEY, 3);

    Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptyMap());
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(HTML_KEY, "https://example.org")
        .put(MAX_POINTS_KEY, 44)
        .put(MAX_SUBMISSIONS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptyMap());
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingMaxPoints() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(NAME_KEY, "another name")
        .put(HTML_KEY, "http://localhost:4567")
        .put(MAX_SUBMISSIONS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptyMap());
  }

  @Test(expected = JSONException.class)
  public void testExerciseFromJsonObjectMissingMaxSubmissions() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(NAME_KEY, "yet another name")
        .put(HTML_KEY, "localhost:1234")
        .put(MAX_POINTS_KEY, 4);

    Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptyMap());
  }

  @Test
  public void testEquals() {
    var info = new SubmissionInfo(Collections.emptyMap());
    var exercise = new Exercise(7, "oneEx", "http://localhost:1111", info, 0, 0, OptionalLong.empty(), "A");
    var sameExercise = new Exercise(7, "twoEx", "http://localhost:2222", info, 3, 4, OptionalLong.empty(), "B");
    var otherExercise = new Exercise(4, "oneEx", "http://localhost:2222", info, 2, 1, OptionalLong.empty(), "A");

    assertEquals(exercise, sameExercise);
    assertEquals(exercise.hashCode(), sameExercise.hashCode());

    assertNotEquals(exercise, otherExercise);
  }

  @Test
  public void testIsCompleted() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise optionalNotSubmitted
        = new Exercise(1, "optionalNotSubmitted", "http://localhost:1111", info, 1, 10, OptionalLong.empty(), "training");
    Exercise optionalSubmitted
        = new Exercise(2, "optionalSubmitted", "http://localhost:1111", info, 1, 10, OptionalLong.empty(), "training");
    optionalSubmitted.addSubmissionResult(new SubmissionResult(
        1, 0, 0.0, SubmissionResult.Status.GRADED, optionalSubmitted));

    assertFalse("Optional assignment with no submissions isn't completed",
        optionalNotSubmitted.isCompleted());
    assertFalse("Optional assignment with submissions isn't completed",
        optionalSubmitted.isCompleted());

    Exercise noSubmissions
        = new Exercise(3, "noSubmissions", "http://localhost:1111", info, 5, 10, OptionalLong.empty(), null);
    Exercise failed
        = new Exercise(4, "failed", "http://localhost:1111", info, 5, 10, OptionalLong.of(1), null);
    failed.addSubmissionResult(new SubmissionResult(
        1, 3, 0.0, SubmissionResult.Status.GRADED, failed));

    Exercise completed = new Exercise(5, "completed", "http://localhost:1111", info, 5, 10, OptionalLong.of(1), null);
    completed.addSubmissionResult(new SubmissionResult(
        1, 5, 0.0, SubmissionResult.Status.GRADED, completed));


    assertFalse("Assignment with no submissions isn't completed",
        noSubmissions.isCompleted());
    assertFalse("Assignment with partial user points isn't completed",
        failed.isCompleted());
    assertTrue("Assignment with full user points is completed",
        completed.isCompleted());
  }

  @Test
  public void testIsOptional() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise optional = new Exercise(1, "optional", "http://localhost:1111", info, 1, 10, OptionalLong.empty(), "training");
    assertTrue("Assignment is optional",
        optional.isOptional());

    Exercise notOptional = new Exercise(2, "notOptional", "http://localhost:1111", info, 1, 10, OptionalLong.empty(), "B");
    assertFalse("Assignment isn't optional",
        notOptional.isOptional());
  }

  @Test
  public void testIsInGrading() {
    var exercise = new Exercise(
        1, "", "http://localhost:1", new SubmissionInfo(Collections.emptyMap()), 10, 10, OptionalLong.empty(), null);
    exercise.addSubmissionResult(new SubmissionResult(
        0, 0, 0.0, SubmissionResult.Status.UNOFFICIAL, exercise
    ));
    assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        1, 0, 0.0, SubmissionResult.Status.GRADED, exercise
    ));
    assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        2, 0, 0.0, SubmissionResult.Status.UNKNOWN, exercise
    ));
    assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        3, 0, 0.0, SubmissionResult.Status.WAITING, exercise
    ));
    assertTrue(exercise.isInGrading());
  }

}
