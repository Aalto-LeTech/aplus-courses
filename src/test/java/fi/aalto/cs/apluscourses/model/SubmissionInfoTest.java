package fi.aalto.cs.apluscourses.model;

import java.util.Arrays;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SubmissionInfoTest {

  @Test
  public void testSubmissionInfo() {
    SubmissionInfo info = new SubmissionInfo(10, new SubmittableFile[] {
        new SubmittableFile("file1"), new SubmittableFile("file2") });
    Assert.assertEquals("The submissions limit is the same as the one given to the constructor",
        10, info.getSubmissionsLimit());
    Assert.assertEquals("The filenames are the same as those given to the constructor",
        "file1", info.getFiles()[0].getName());
    Assert.assertEquals("The filenames are the same as those given to the constructor",
        "file2", info.getFiles()[1].getName());
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

    SubmissionInfo info = SubmissionInfo.fromJsonObject(json);

    Assert.assertEquals("The submissions limit is the same as that in the JSON",
        13, info.getSubmissionsLimit());
    Assert.assertEquals("The filenames are parsed from the JSON",
        "coolFilename.scala", info.getFiles()[0].getName());
  }

}
