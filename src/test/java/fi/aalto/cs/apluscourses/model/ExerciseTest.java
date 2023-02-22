package fi.aalto.cs.apluscourses.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExerciseTest {

  @Test
  void testExercise() {
    var info = new SubmissionInfo(Map.of("de", List.of(new SubmittableFile("f1", "a"))));
    Exercise exercise = new Exercise(987, "def", "http://localhost:4444", info, 15, 10,
        OptionalLong.empty(), null, false);

    Assertions.assertEquals(987L, exercise.getId(), "The ID is the same as the one given to the constructor");
    Assertions.assertEquals("def", exercise.getName(), "The name is the same as the one given to the constructor");
    Assertions.assertEquals("http://localhost:4444", exercise.getHtmlUrl(),
        "The HTML URL is the same as the one given to the constructor");
    Assertions.assertEquals(15, exercise.getMaxPoints(),
        "The maximum points are the same as those given to the constructor");
    Assertions.assertEquals(10, exercise.getMaxSubmissions(),
        "The maximum submissions are the same as those given to the constructor");
    Assertions.assertEquals("", exercise.getDifficulty(),
        "The difficulty should be converted from null to an empty string");
    Assertions.assertTrue(exercise.isSubmittable(),
        "The exercise submittability depends on the submission info parameter");
    Assertions.assertNull(exercise.getBestSubmission(), "The submission ID is the one given to the constructor");
    Assertions.assertFalse(exercise.isInGrading(), "The exercise is In Grading (default value)");
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
  void testExerciseFromJsonObject() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 11L)
        .put(NAME_KEY, "Cool name")
        .put(HTML_KEY, "http://localhost:1000")
        .put(MAX_POINTS_KEY, 99)
        .put(MAX_SUBMISSIONS_KEY, 5)
        .put(DIFFICULTY_KEY, "ha")
        .put("additional key", "which shouldn't cause errors");

    Exercise exercise = Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptySet(), Collections.emptyMap());
    exercise.addSubmissionResult(
        new SubmissionResult(2, 0, 0.0, SubmissionResult.Status.GRADED, exercise));

    Assertions.assertEquals(11L, exercise.getId(), "The ID is the same as the one in the JSON object");
    Assertions.assertEquals("Cool name", exercise.getName(), "The name is the same as the one in the JSON object");
    Assertions.assertEquals("http://localhost:1000", exercise.getHtmlUrl(),
        "The HTML URL is the same as the one in the JSON object");
    Assertions.assertEquals(2L, exercise.getBestSubmission().getId(),
        "The best submission is read from the points object");
    Assertions.assertEquals(99, exercise.getMaxPoints(), "The max points is the same as the one in the JSON object");
    Assertions.assertEquals(5, exercise.getMaxSubmissions(),
        "The max submissions is the same as the one in the JSON object");
    Assertions.assertEquals("ha", exercise.getDifficulty(), "The difficulty is the same as the one in the JSON object");
    Assertions.assertFalse(exercise.isSubmittable(), "The missing exercise_info makes the exercise unsubmittable");
    Assertions.assertFalse(exercise.isInGrading(), "The exercise is In Grading (default value)");
  }

  @Test
  void testExerciseFromJsonObjectMissingId() {
    JSONObject json = new JSONObject()
        .put(NAME_KEY, "A name")
        .put(HTML_KEY, "https://example.com")
        .put(MAX_POINTS_KEY, 55)
        .put(MAX_SUBMISSIONS_KEY, 3);

    assertThrows(JSONException.class, () ->
        Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptySet(), Collections.emptyMap()));
  }

  @Test
  void testExerciseFromJsonObjectMissingName() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(HTML_KEY, "https://example.org")
        .put(MAX_POINTS_KEY, 44)
        .put(MAX_SUBMISSIONS_KEY, 4);

    assertThrows(JSONException.class, () ->
        Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptySet(), Collections.emptyMap()));
  }

  @Test
  void testExerciseFromJsonObjectMissingMaxPoints() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(NAME_KEY, "another name")
        .put(HTML_KEY, "http://localhost:4567")
        .put(MAX_SUBMISSIONS_KEY, 4);

    assertThrows(JSONException.class, () ->
        Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptySet(), Collections.emptyMap()));
  }

  @Test
  void testExerciseFromJsonObjectMissingMaxSubmissions() {
    JSONObject json = new JSONObject()
        .put(ID_KEY, 357)
        .put(NAME_KEY, "yet another name")
        .put(HTML_KEY, "localhost:1234")
        .put(MAX_POINTS_KEY, 4);

    assertThrows(JSONException.class, () ->
        Exercise.fromJsonObject(json, TEST_POINTS, Collections.emptySet(), Collections.emptyMap()));
  }

  @Test
  void testEquals() {
    var info = new SubmissionInfo(Collections.emptyMap());
    var exercise = new Exercise(7, "oneEx", "http://localhost:1111", info, 0, 0, OptionalLong.empty(), "A", false);
    var sameExercise = new Exercise(7, "twoEx", "http://localhost:2222", info, 3, 4,
        OptionalLong.empty(), "B", false);
    var otherExercise = new Exercise(4, "oneEx", "http://localhost:2222", info, 2, 1,
        OptionalLong.empty(), "A", false);

    Assertions.assertEquals(exercise, sameExercise);
    Assertions.assertEquals(exercise.hashCode(), sameExercise.hashCode());

    Assertions.assertNotEquals(exercise, otherExercise);
  }

  @Test
  void testIsCompleted() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise optionalNotSubmitted = new Exercise(1, "optionalNotSubmitted",
        "http://localhost:1111", info, 1, 10, OptionalLong.empty(), "training", true);
    Exercise optionalSubmitted = new Exercise(2, "optionalSubmitted", "http://localhost:1111",
        info, 1, 10, OptionalLong.empty(), "training", true);
    optionalSubmitted.addSubmissionResult(new SubmissionResult(
        1, 0, 0.0, SubmissionResult.Status.GRADED, optionalSubmitted));

    Assertions.assertFalse(optionalNotSubmitted.isCompleted(),
        "Optional assignment with no submissions isn't completed");
    Assertions.assertFalse(optionalSubmitted.isCompleted(), "Optional assignment with submissions isn't completed");

    Exercise noSubmissions
        = new Exercise(3, "noSubmissions", "http://localhost:1111", info, 5, 10, OptionalLong.empty(), null, false);
    Exercise failed
        = new Exercise(4, "failed", "http://localhost:1111", info, 5, 10, OptionalLong.of(1), null, false);
    failed.addSubmissionResult(new SubmissionResult(
        1, 3, 0.0, SubmissionResult.Status.GRADED, failed));

    Exercise completed = new Exercise(5, "completed", "http://localhost:1111", info, 5, 10,
        OptionalLong.of(1), null, false);
    completed.addSubmissionResult(new SubmissionResult(
        1, 5, 0.0, SubmissionResult.Status.GRADED, completed));


    Assertions.assertFalse(noSubmissions.isCompleted(), "Assignment with no submissions isn't completed");
    Assertions.assertFalse(failed.isCompleted(), "Assignment with partial user points isn't completed");
    Assertions.assertTrue(completed.isCompleted(), "Assignment with full user points is completed");
  }

  @Test
  void testIsOptional() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise optional =
        new Exercise(1, "optional", "http://localhost:1111", info, 1, 10, OptionalLong.empty(), "training", true);
    Assertions.assertTrue(optional.isOptional(), "Assignment is optional");

    Exercise notOptional =
        new Exercise(2, "notOptional", "http://localhost:1111", info, 1, 10, OptionalLong.empty(), "B", false);
    Assertions.assertFalse(notOptional.isOptional(), "Assignment isn't optional");
  }

  @Test
  void testIsInGrading() {
    var exercise = new Exercise(
        1, "", "http://localhost:1", new SubmissionInfo(Collections.emptyMap()), 10, 10,
        OptionalLong.empty(), null, false);
    exercise.addSubmissionResult(new SubmissionResult(
        0, 0, 0.0, SubmissionResult.Status.UNOFFICIAL, exercise
    ));
    Assertions.assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        1, 0, 0.0, SubmissionResult.Status.GRADED, exercise
    ));
    Assertions.assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        2, 0, 0.0, SubmissionResult.Status.UNKNOWN, exercise
    ));
    Assertions.assertFalse(exercise.isInGrading());
    exercise.addSubmissionResult(new SubmissionResult(
        3, 0, 0.0, SubmissionResult.Status.WAITING, exercise
    ));
    Assertions.assertTrue(exercise.isInGrading());
  }

}
