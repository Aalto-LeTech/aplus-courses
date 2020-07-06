package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import org.junit.Test;

public class SubmissionsDashboardTest {

  @Test
  public void testGetSubmissionsResults() throws IOException {
    String token = "92384573df6ba9e45ac6297cc2b23a38592c90f7";
    char[] charSetToken = token.toCharArray();
    SubmissionsDashboard submissionsDashboard = SubmissionsDashboard
        .getSubmissionsDashboard(163, new APlusAuthentication(charSetToken));
  }
}
