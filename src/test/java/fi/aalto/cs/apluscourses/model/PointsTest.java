package fi.aalto.cs.apluscourses.model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PointsTest {

  @Test
  public void testPoints() {
    Map<Long, Integer> submissionPoints = new HashMap<>();
    submissionPoints.put(11L, 44);
    submissionPoints.put(22L, 55);
    Points points = new Points(
        Collections.singletonMap(123L, Arrays.asList(11L, 22L)),
        Collections.singletonMap(123L, 55),
        submissionPoints
    );

    Assert.assertEquals("The submission ID list is the same as that given to the constructor",
        Long.valueOf(11L), points.getSubmissions().get(123L).get(0));
    Assert.assertEquals("The submission ID list is the same as that given to the constructor",
        Long.valueOf(22L), points.getSubmissions().get(123L).get(1));
    Assert.assertEquals("The points for an exercise is the same as that given to the constructor",
        Integer.valueOf(55), points.getExercisePoints().get(123L));
    Assert.assertEquals("The submission points are the same as those given to the constructor",
        Integer.valueOf(44), points.getSubmissionPoints().get(11L));
    Assert.assertEquals("The submission points are the same as those given to the constructor",
        Integer.valueOf(55), points.getSubmissionPoints().get(22L));
  }

  @Test
  public void testFromJsonObject() {
    JSONArray submissionsWithPoints1 = new JSONArray()
        .put(new JSONObject().put("id", 1L).put("grade", 10))
        .put(new JSONObject().put("id", 2L).put("grade", 20));
    JSONArray submissionsWithPoints2 = new JSONArray()
        .put(new JSONObject().put("id", 3L).put("grade", 30))
        .put(new JSONObject().put("id", 4L).put("grade", 40));

    JSONArray exercises = new JSONArray()
        .put(new JSONObject()
            .put("id", 100L)
            .put("points", 20)
            .put("submissions_with_points", submissionsWithPoints1))
        .put(new JSONObject()
            .put("id", 200L)
            .put("points", 40)
            .put("submissions_with_points", submissionsWithPoints2));

    JSONArray modules = new JSONArray()
        .put(new JSONObject().put("exercises", exercises));

    JSONObject json = new JSONObject().put("modules", modules);

    Points points = Points.fromJsonObject(json);

    Assert.assertEquals("The exercise points are parsed from the JSON",
        Integer.valueOf(20), points.getExercisePoints().get(100L));
    Assert.assertEquals("The exercise points are parsed from the JSON",
        Integer.valueOf(40), points.getExercisePoints().get(200L));

    Assert.assertEquals("The submission IDs are parsed from the JSON (in reverse order)",
        Long.valueOf(2L), points.getSubmissions().get(100L).get(0));
    Assert.assertEquals("The submission IDs are parsed from the JSON (in reverse order)",
        Long.valueOf(1L), points.getSubmissions().get(100L).get(1));
    Assert.assertEquals("The submission IDs are parsed from the JSON (in reverse order)",
        Long.valueOf(4L), points.getSubmissions().get(200L).get(0));
    Assert.assertEquals("The submission IDs are parsed from the JSON (in reverse order)",
        Long.valueOf(3L), points.getSubmissions().get(200L).get(1));

    Assert.assertEquals("The submission points are parsed from the JSON",
        Integer.valueOf(10), points.getSubmissionPoints().get(1L));
    Assert.assertEquals("The submission points are parsed from the JSON",
        Integer.valueOf(20), points.getSubmissionPoints().get(2L));
    Assert.assertEquals("The submission points are parsed from the JSON",
        Integer.valueOf(30), points.getSubmissionPoints().get(3L));
    Assert.assertEquals("The submission points are parsed from the JSON",
        Integer.valueOf(40), points.getSubmissionPoints().get(4L));
  }
}
