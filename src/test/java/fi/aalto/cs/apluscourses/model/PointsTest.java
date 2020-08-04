package fi.aalto.cs.apluscourses.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class PointsTest {

  @Test
  public void testPoints() {
    Points points = new Points(
        Collections.singletonMap(123L, Arrays.asList(11L, 22L)),
        Collections.singletonMap(123L, 55)
    );

    Assert.assertEquals("The submission ID list is the same as that given to the constructor",
        Long.valueOf(11L), points.getSubmissions().get(123L).get(0));
    Assert.assertEquals("The submission ID list is the same as that given to the constructor",
        Long.valueOf(22L), points.getSubmissions().get(123L).get(1));
    Assert.assertEquals("The points for an exercise is the same as that given to the constructor",
        Integer.valueOf(55), points.getPoints().get(123L));
  }

  @Test
  public void testFromJsonObject() {
    String jsonObjectString = getLargeJsonString();
    JSONObject jsonObject = new JSONObject(jsonObjectString);

    Points points = Points.fromJsonObject(jsonObject);
    Map<Long, Integer> idToPoints = points.getPoints();

    int[] exercisePoints = new int[] {
        idToPoints.get(26038L),
        idToPoints.get(26039L),
        idToPoints.get(26160L),
        idToPoints.get(26162L)
    };

    Assert.assertArrayEquals("The points of exercises are the same as those in the JSON",
        new int[] {5, 2, 0, 0}, exercisePoints);

    Assert.assertEquals("The submissions for exercises are the same as those in the JSON reversed",
        Long.valueOf(2921874L), points.getSubmissions().get(26038L).get(2));
    Assert.assertEquals("The submissions for exercises are the same as those in the JSON reversed",
        Long.valueOf(2921867L), points.getSubmissions().get(26038L).get(1));
  }

  @NotNull
  @Contract(pure = true)
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
