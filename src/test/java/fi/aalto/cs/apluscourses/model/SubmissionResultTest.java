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
        "[\n"
        + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921874/\",\n"
        + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921867/\",\n"
        + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921858/\"\n"
        + "]";
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
        "{\n"
        + "passed: true,\n"
        + "official: true,\n"
        + "difficulty: \"A\",\n"
        + "name: \"|fi:Tehtävä 1 (Piazza)|en:Assignment 1 (Piazza)|\",\n"
        + "submissions: [\n"
        + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921874/?format=json\",\n"
        + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921867/?format=json\",\n"
        + "\"https://plus.cs.aalto.fi/api/v2/submissions/2921858/?format=json\"\n"
        + "],\n"
        + "submission_count: 3,\n"
        + "points_to_pass: 0,\n"
        + "id: 26038,\n"
        + "points: 5,\n"
        + "best_submission: \"https://plus.cs.aalto.fi/api/v2/submissions/2921874/?format=json\",\n"
        + "url: \"https://plus.cs.aalto.fi/api/v2/exercises/26038/?format=json\",\n"
        + "max_points: 5\n"
        + "}";
    JSONObject jsonObject = new JSONObject(jsonObjectString);

    //when
    SubmissionResult submissionResult = SubmissionResult.fromJsonObject(jsonObject);

    //  then
    assertEquals("Exercise id is correct.", 26038, submissionResult.getExerciseId());
    assertEquals("Exercise amount of submissions (via IDs) is correct.", 3, submissionResult.getSubmissionIds().size());
    assertEquals("Amount of made submissions is correct.", 3, submissionResult.getSubmissionsCount());
    assertEquals("The required to pass amount of points is correct.", 0, submissionResult.getPointsToPass());
    assertEquals("The amount of points received so far is correct.", 5, submissionResult.getPoints());
    assertEquals("The amount of maximum possible points is correct.", 5, submissionResult.getMaxPoints());
  }
}