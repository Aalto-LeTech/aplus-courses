package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

public class SubmissionsDashboardTest {

  @Test
  public void testFromJsonObject() {
    //  given
    String jsonObjectString = getLargeJsonString();
    JSONObject jsonObject = new JSONObject(jsonObjectString);

    //  when
    SubmissionsDashboard submissionsDashboard = SubmissionsDashboard.fromJsonObject(jsonObject);

    //  then
    assertEquals("Student id is correct.", 19457, submissionsDashboard.getStudentId());
    assertEquals("List of SubmissionResults is of a correct length.", 4,
        submissionsDashboard.getSubmissionResults().size());
    assertEquals("The total amount of points is correct.", 5,
        submissionsDashboard.getTotalPoints());
  }

  private String getLargeJsonString() {
    return "{\"id\":19457,\"url\":\"https://plus.cs.aalto.fi/api/v2/users/19457/?format=json\","
        + "\"username\":\"testik34@aalto.fi\",\"student_id\":\"TESTI64\","
        + "\"email\":\"19457@localhost\",\"full_name\":\"KuusikymmentÃ¤neljÃ¤TESTI-Opiskelija\","
        + "\"is_external\":false,\"tags\":[],\"submission_count\":3,"
        + "\"points\":5,\"points_by_difficulty\":{\"A\":5},"
        + "\"modules\":[{\"exercises\":[{\"passed\":true,\"official\":true,\"difficulty\":\"A\","
        + "\"name\":\"|fi:TehtÃ¤vÃ¤1(Piazza)|en:Assignment1(Piazza)|\","
        + "\"submissions\":[\"https://plus.cs.aalto.fi/api/v2/submissions/2921874/?format=json\","
        + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921867/?format=json\","
        + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921858/?format=json\"],"
        + "\"submission_count\":3,\"points_to_pass\":0,\"id\":26038,\"points\":5,"
        + "\"best_submission\":\"https://plus.cs.aalto.fi/api/v2/submissions/2921874/?format=json\","
        + "\"url\":\"https://plus.cs.aalto.fi/api/v2/exercises/26038/?format=json\","
        + "\"max_points\":5},{\"passed\":true,\"official\":true,\"difficulty\":\"\","
        + "\"name\":\"|fi:Palaute|en:Feedback|\","
        + "\"submissions\":[\"https://plus.cs.aalto.fi/api/v2/submissions/2921872/?format=json\"],"
        + "\"submission_count\":1,\"points_to_pass\":1,\"id\":26039,\"points\":2,"
        + "\"best_submission\":\"https://plus.cs.aalto.fi/api/v2/submissions/2921872/?format=json\","
        + "\"url\":\"https://plus.cs.aalto.fi/api/v2/exercises/26039/?format=json\","
        + "\"max_points\":2}]},{\"exercises\":[{\"passed\":false,\"official\":false,"
        + "\"difficulty\":\"\",\"name\":\"|fi:Palaute|en:Feedback|\",\"submissions\":[],"
        + "\"submission_count\":0,\"points_to_pass\":1,\"id\":26160,\"points\":0,"
        + "\"best_submission\":null,"
        + "\"url\":\"https://plus.cs.aalto.fi/api/v2/exercises/26160/?format=json\","
        + "\"max_points\":2},{\"passed\":true,\"official\":false,\"difficulty\":\"A\","
        + "\"name\":\"|fi:TehtÃ¤vÃ¤1(FlappyBug5)|en:Assignment1(FlappyBug5)|\",\"submissions\":[],"
        + "\"submission_count\":0,\"points_to_pass\":0,\"id\":26162,\"points\":0,"
        + "\"best_submission\":null,"
        + "\"url\":\"https://plus.cs.aalto.fi/api/v2/exercises/26162/?format=json\","
        + "\"max_points\":10}]}]}";
  }
}
