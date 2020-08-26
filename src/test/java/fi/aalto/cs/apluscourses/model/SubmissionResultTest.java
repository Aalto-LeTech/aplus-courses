package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

public class SubmissionResultTest {

  @Test
  public void testSubmissionResult() {
    SubmissionResult submissionResult
        = new SubmissionResult(123L, 13, "http://example.com/", SubmissionResult.Status.GRADED);
    assertEquals("The ID is the same as the one given to the constructor",
        123L, submissionResult.getId());
    assertEquals("The grade is the same as the one given to the constructor",
        13, submissionResult.getPoints());
    assertEquals("The status is the samme as the one given to the constructor",
        SubmissionResult.Status.GRADED, submissionResult.getStatus());
    assertEquals("The submission URL is correct", "http://example.com/submissions/123/",
        submissionResult.getUrl());
  }

  @Test
  public void testFromJsonObject() {
    JSONObject jsonObject = new JSONObject()
        .put("id", 234)
        .put("grade", 30)
        .put("exercise", new JSONObject()
            .put("html_url", "https://example.com/"))
        .put("status", "ready");
    SubmissionResult submissionResult = SubmissionResult.fromJsonObject(jsonObject);

    assertEquals("The ID is the same as the one in the JSON object",
        234L, submissionResult.getId());
    assertEquals("The grade is the same as the one in the JSON object",
        30, submissionResult.getPoints());
    assertEquals("The status is parsed correctly from the JSON object",
        SubmissionResult.Status.GRADED, submissionResult.getStatus());
    assertEquals("The exercise URL is taken correctly from the JSON object",
        "https://example.com/submissions/234/", submissionResult.getUrl());
  }

}