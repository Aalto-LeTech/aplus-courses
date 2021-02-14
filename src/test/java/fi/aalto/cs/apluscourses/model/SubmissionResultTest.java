package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.json.JSONObject;
import org.junit.Test;

public class SubmissionResultTest {

  @Test
  public void testSubmissionResult() {
    Exercise exercise = new Exercise(444L, "someEx", "http://example.com/", 15, 20, 10, true, null);
    SubmissionResult submissionResult
        = new SubmissionResult(123L, 13, SubmissionResult.Status.GRADED, exercise, 0.5);
    assertEquals("The ID is the same as the one given to the constructor",
        123L, submissionResult.getId());
    assertEquals("The grade is the same as the one given to the constructor",
        13, submissionResult.getPoints());
    assertEquals("The status is the same as the one given to the constructor",
        SubmissionResult.Status.GRADED, submissionResult.getStatus());
    assertEquals("The late penalty is the same as the one given to the constructor",
        0.5, submissionResult.getLatePenalty(), 0.01);
    assertEquals("The submission URL is correct", "http://example.com/submissions/123/",
        submissionResult.getHtmlUrl());
    assertSame(exercise, submissionResult.getExercise());
  }

  @Test
  public void testFromJsonObject() {
    Exercise exercise = new Exercise(555L, "myEx", "https://example.org/", 15, 20, 10, true, null);
    JSONObject jsonObject = new JSONObject()
        .put("id", 234)
        .put("grade", 30)
        .put("exercise", new JSONObject()
            .put("html_url", "https://example.com/"))
        .put("status", "ready")
        .put("late_penalty_applied", 0.6);
    SubmissionResult submissionResult = SubmissionResult.fromJsonObject(jsonObject, exercise);

    assertEquals("The ID is the same as the one in the JSON object",
        234L, submissionResult.getId());
    assertEquals("The grade is the same as the one in the JSON object",
        30, submissionResult.getPoints());
    assertEquals("The status is parsed correctly from the JSON object",
        SubmissionResult.Status.GRADED, submissionResult.getStatus());
    assertEquals("The exercise URL is taken correctly from the JSON object",
        "https://example.org/submissions/234/", submissionResult.getHtmlUrl());
    assertEquals("The late penalty is the same as the one in the JSON object",
        0.6, submissionResult.getLatePenalty(), 0.01);
  }

}
