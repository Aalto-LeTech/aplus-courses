package fi.aalto.cs.apluscourses.model;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.OptionalLong;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubmissionResultTest {

  @Test
  void testSubmissionResult() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise = new Exercise(444L, "someEx", "http://example.com/", info, 20, 10,
        OptionalLong.of(123L), null, false);
    SubmissionResult submissionResult
        = new SubmissionResult(123L, 13, 0.5, SubmissionResult.Status.GRADED, exercise);
    exercise.addSubmissionResult(submissionResult);
    Assertions.assertEquals(123L, submissionResult.getId(), "The ID is the same as the one given to the constructor");
    Assertions.assertEquals(13, submissionResult.getPoints(),
        "The grade is the same as the one given to the constructor");
    Assertions.assertEquals(exercise.getUserPoints(), submissionResult.getPoints(),
        "The result points are the same as the exercise points");
    Assertions.assertEquals(SubmissionResult.Status.GRADED, submissionResult.getStatus(),
        "The status is the same as the one given to the constructor");
    Assertions.assertEquals(0.5, submissionResult.getLatePenalty(), 0.01,
        "The late penalty is the same as the one given to the constructor");
    Assertions.assertEquals("http://example.com/submissions/123/", submissionResult.getHtmlUrl(),
        "The submission URL is correct");
    Assertions.assertSame(exercise, submissionResult.getExercise());
  }

  @Test
  void testFromJsonObject() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise = new Exercise(555L, "myEx", "https://example.org/", info, 20, 10,
        OptionalLong.of(234), null, false);
    JSONObject jsonObject = new JSONObject()
        .put("id", 234)
        .put("grade", 30)
        .put("exercise", new JSONObject()
            .put("html_url", "https://example.com/"))
        .put("status", "ready")
        .put("late_penalty_applied", 0.6)
        .put("files", new JSONArray());
    SubmissionResult submissionResult = SubmissionResult.fromJsonObject(jsonObject, exercise, mock(Course.class));
    exercise.addSubmissionResult(submissionResult);

    Assertions.assertEquals(234L, submissionResult.getId(), "The ID is the same as the one in the JSON object");
    Assertions.assertEquals(30, submissionResult.getPoints(), "The grade is the same as the one in the JSON object");
    Assertions.assertEquals(SubmissionResult.Status.GRADED, submissionResult.getStatus(),
        "The status is parsed correctly from the JSON object");
    Assertions.assertEquals("https://example.org/submissions/234/", submissionResult.getHtmlUrl(),
        "The exercise URL is taken correctly from the JSON object");
    Assertions.assertEquals(0.6, submissionResult.getLatePenalty(), 0.01,
        "The late penalty is the same as the one in the JSON object");
  }

}
