package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class SubmissionResultTest {

  @Test
  public void testGetSubmissionIdsFromJsonArray() {
    //  given
    String jsonArrayString =
        "[\"https://plus.cs.aalto.fi/api/v2/submissions/2921874/\","
            + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921867/\","
            + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921858/\"]";
    JSONArray jsonArray = new JSONArray(jsonArrayString);

    // when
    List<Integer> submissionIds = SubmissionResult.getSubmissionIdsFromJsonArray(jsonArray);

    //then
    assertTrue("List contains the id: 2921874.", submissionIds.contains(2921874));
    assertTrue("List contains the id: 2921867.", submissionIds.contains(2921867));
    assertTrue("List contains the id: 2921858.", submissionIds.contains(2921858));
    assertEquals("List contains exactly 3 items.", 3, submissionIds.size());
  }

  @Test
  public void testFromJsonObject() {
    //  given
    String jsonObjectString =
        "{passed:true,official:true,difficulty:\"A\","
            + "name:\"|fi:Tehtävä1(Piazza)|en:Assignment1(Piazza)|\","
            + "submissions:[\"https://plus.cs.aalto.fi/api/v2/submissions/2921874/?format=json\","
            + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921867/?format=json\","
            + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921858/?format=json\"],"
            + "submission_count:3,points_to_pass:0,id:26038,points:5,"
            + "best_submission:\"https://plus.cs.aalto.fi/api/v2/submissions/2921874/?format=json\","
            + "url:\"https://plus.cs.aalto.fi/api/v2/exercises/26038/?format=json\",max_points:5}";
    JSONObject jsonObject = new JSONObject(jsonObjectString);

    //when
    SubmissionResult submissionResult = SubmissionResult.fromJsonObject(jsonObject);

    //  then
    assertEquals("Exercise id is correct.", 26038, submissionResult.getExerciseId());
    assertEquals("Exercise amount of submissions (via IDs) is correct.", 3,
        submissionResult.getSubmissionIds().size());
    assertEquals("Amount of made submissions is correct.", 3,
        submissionResult.getSubmissionsCount());
    assertEquals("The required to pass amount of points is correct.", 0,
        submissionResult.getPointsToPass());
    assertEquals("The amount of points received so far is correct.", 5,
        submissionResult.getPoints());
    assertEquals("The amount of maximum possible points is correct.", 5,
        submissionResult.getMaxPoints());
  }
}