package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubmissionResultTest {

  @Test
  public void testSubmissionResult() {
    SubmissionResult submissionResult = new SubmissionResult(123L, 1);
    assertEquals("The ID is the same as the one given to the constructor",
        123L, submissionResult.getId());
    assertEquals("The submission number is the same as the one given to the constructor",
        1, submissionResult.getSubmissionNumber());
  }

}