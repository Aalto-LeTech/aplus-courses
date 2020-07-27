package fi.aalto.cs.apluscourses.model;

import junit.framework.Assert;
import org.json.JSONObject;
import org.junit.Test;

public class SubmissionHistoryTest {

  @Test
  public void testSubmissionHistory() {
    SubmissionHistory submissionHistory = new SubmissionHistory(33);
    Assert.assertEquals("The number of submissions should be the number given to the constructor",
        33, submissionHistory.getNumberOfSubmissions());
  }

  @Test
  public void testFromJsonObject() {
    JSONObject json = new JSONObject()
        .put("count", 99)
        .put("something", "else");
    SubmissionHistory submissionHistory = SubmissionHistory.fromJsonObject(json);
    Assert.assertEquals("The number of submissions should be the number in the JSON",
        99, submissionHistory.getNumberOfSubmissions());
  }

}
