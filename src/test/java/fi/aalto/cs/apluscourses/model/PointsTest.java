package fi.aalto.cs.apluscourses.model;

import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class PointsTest {

  @Test
  public void testPoints() {
    Points points = new Points(
        Collections.singletonMap(51L, List.of(0L, 123L)),
        Collections.singletonMap(123L, List.of(11L, 22L)),
        Collections.singletonMap(123L, 55)
    );

    Assert.assertEquals("The exercise ID list is the same as that given to the constructor",
        Long.valueOf(0L), points.getExercises(51L).get(0));
    Assert.assertEquals("The exercise ID list is the same as that given to the constructor",
        Long.valueOf(123L), points.getExercises(51L).get(1));
    Assert.assertEquals("The submission ID list is the same as that given to the constructor",
        Long.valueOf(11L), points.getSubmissions(123L).get(0));
    Assert.assertEquals("The submission ID list is the same as that given to the constructor",
        Long.valueOf(22L), points.getSubmissions(123L).get(1));
    Assert.assertEquals("The points for an exercise is the same as that given to the constructor",
        55, points.getExercisePoints(123L));
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
        .put(new JSONObject()
            .put("id", 0L)
            .put("exercises", exercises));

    JSONObject json = new JSONObject().put("modules", modules);

    Points points = Points.fromJsonObject(json);

    Assert.assertEquals("The exercise IDs are parsed from the JSON",
        List.of(100L, 200L), points.getExercises(0));

    Assert.assertEquals("The exercise points are parsed from the JSON",
        20, points.getExercisePoints(100L));
    Assert.assertEquals("The exercise points are parsed from the JSON",
        40, points.getExercisePoints(200L));

    Assert.assertEquals("The submission IDs are parsed from the JSON (in reverse order)",
        Long.valueOf(2L), points.getSubmissions(100L).get(0));
    Assert.assertEquals("The submission IDs are parsed from the JSON (in reverse order)",
        Long.valueOf(1L), points.getSubmissions(100L).get(1));
    Assert.assertEquals("The submission IDs are parsed from the JSON (in reverse order)",
        Long.valueOf(4L), points.getSubmissions(200L).get(0));
    Assert.assertEquals("The submission IDs are parsed from the JSON (in reverse order)",
        Long.valueOf(3L), points.getSubmissions(200L).get(1));
  }
}
