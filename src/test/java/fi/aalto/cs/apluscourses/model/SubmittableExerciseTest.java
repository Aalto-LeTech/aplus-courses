package fi.aalto.cs.apluscourses.model;

import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SubmittableExerciseTest {

  @Test
  public void testSubmittableExercise() {
    SubmittableExercise exercise = new SubmittableExercise(123, "yay", 10,
        Arrays.asList(new SubmittableFile("file1"), new SubmittableFile("file2")));
    Assert.assertEquals("The ID is the same as the one given to the constructor",
        123, exercise.getId());
    Assert.assertEquals("The name is the same as the one given to the constructor",
        "yay", exercise.getName());
    Assert.assertEquals("The submissions limit is the same as the one given to the constructor",
        10, exercise.getSubmissionsLimit());
    Assert.assertEquals("The filenames are the same as those given to the constructor",
        "file1", exercise.getFiles().get(0));
    Assert.assertEquals("The filenames are the same as those given to the constructor",
        "file2", exercise.getFiles().get(1));
  }

  @Test
  public void testFromJsonObject() {
    JSONArray formSpec = new JSONArray()
        .put(new JSONObject()
            .put("title", "i18n_coolFilename.scala")
            .put("type", "file"))
        .put(new JSONObject()
            .put("title", "ignored because")
            .put("type", "is not file"));

    JSONObject formLocalization = new JSONObject()
        .put("i18n_coolFilename.scala", new JSONObject()
            .put("en", "coolFilename.scala"));

    JSONObject exerciseInfo = new JSONObject()
        .put("form_spec", formSpec)
        .put("form_i18n", formLocalization);

    JSONObject json = new JSONObject()
        .put("id", 321)
        .put("name", "test exercise")
        .put("exercise_info", exerciseInfo)
        .put("max_submissions", 13);

    SubmittableExercise exercise = SubmittableExercise.fromJsonObject(json);

    Assert.assertEquals("The ID is the same as that in the JSON", 321, exercise.getId());
    Assert.assertEquals("The name is the same as that in the JSON",
        "test exercise", exercise.getName());
    Assert.assertEquals("The submissions limit is the same as that in the JSON",
        13, exercise.getSubmissionsLimit());
    Assert.assertEquals("The filenames are parsed from the JSON",
        "coolFilename.scala", exercise.getFiles().get(0));
  }

}
