package fi.aalto.cs.apluscourses.model;

import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PointsTest {

  @Test
  void testPoints() {
    Points points = new Points(
        Collections.singletonMap(51L, List.of(0L, 123L)),
        Collections.singletonMap(123L, List.of(11L, 22L)),
        Collections.singletonMap(123L, 22L)
    );

    Assertions.assertEquals(Long.valueOf(0L), points.getExercises(51L).get(0),
        "The exercise ID list is the same as that given to the constructor");
    Assertions.assertEquals(Long.valueOf(123L), points.getExercises(51L).get(1),
        "The exercise ID list is the same as that given to the constructor");
    Assertions.assertEquals(Long.valueOf(11L), points.getSubmissions(123L).get(0),
        "The submission ID list is the same as that given to the constructor");
    Assertions.assertEquals(Long.valueOf(22L), points.getSubmissions(123L).get(1),
        "The submission ID list is the same as that given to the constructor");
  }

  @Test
  void testFromJsonObject() {
    JSONArray submissionsWithPoints1 = new JSONArray()
        .put(new JSONObject().put("id", 1L).put("grade", 10))
        .put(new JSONObject().put("id", 2L).put("grade", 20));
    JSONArray submissionsWithPoints2 = new JSONArray()
        .put(new JSONObject().put("id", 3L).put("grade", 30))
        .put(new JSONObject().put("id", 4L).put("grade", 40));

    JSONArray exercises = new JSONArray()
        .put(new JSONObject()
            .put("id", 100L)
            .put("submissions_with_points", submissionsWithPoints1))
        .put(new JSONObject()
            .put("id", 200L)
            .put("submissions_with_points", submissionsWithPoints2));

    JSONArray modules = new JSONArray()
        .put(new JSONObject()
            .put("id", 0L)
            .put("exercises", exercises));

    JSONObject json = new JSONObject().put("modules", modules);

    Points points = Points.fromJsonObject(json);

    Assertions.assertEquals(List.of(100L, 200L), points.getExercises(0), "The exercise IDs are parsed from the JSON");

    Assertions.assertEquals(Long.valueOf(2L), points.getSubmissions(100L).get(0),
        "The submission IDs are parsed from the JSON (in reverse order)");
    Assertions.assertEquals(Long.valueOf(1L), points.getSubmissions(100L).get(1),
        "The submission IDs are parsed from the JSON (in reverse order)");
    Assertions.assertEquals(Long.valueOf(4L), points.getSubmissions(200L).get(0),
        "The submission IDs are parsed from the JSON (in reverse order)");
    Assertions.assertEquals(Long.valueOf(3L), points.getSubmissions(200L).get(1),
        "The submission IDs are parsed from the JSON (in reverse order)");
  }
}
