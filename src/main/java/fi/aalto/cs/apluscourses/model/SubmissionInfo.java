package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class SubmissionInfo {

  private final int submissionsLimit;

  @NotNull
  private final SubmittableFile[] files;

  /**
   * Construct a submission info instance with the given submission limit and files.
   */
  public SubmissionInfo(int submissionsLimit, @NotNull SubmittableFile[] files) {
    this.submissionsLimit = submissionsLimit;
    this.files = files;
  }

  /**
   * Construct a submission info instance from the given JSON object.
   */
  @NotNull
  public static SubmissionInfo fromJsonObject(@NotNull JSONObject jsonObject) {
    JSONObject exerciseInfo = jsonObject.getJSONObject("exercise_info");
    JSONArray formSpec = exerciseInfo.getJSONArray("form_spec");
    JSONObject localizationInfo = exerciseInfo.getJSONObject("form_i18n");
    List<SubmittableFile> files = new ArrayList<>(formSpec.length());

    for (int i = 0; i < formSpec.length(); ++i) {
      JSONObject spec = formSpec.getJSONObject(i);
      String type = spec.getString("type");
      if (!type.equals("file")) {
        continue;
      }

      String title = spec.getString("title");
      String englishFilename = localizationInfo
          .getJSONObject(title)
          .getString("en");
      files.add(new SubmittableFile(englishFilename));
    }

    int submissionLimit = jsonObject.getInt("max_submissions");

    return new SubmissionInfo(submissionLimit, files.toArray(new SubmittableFile[0]));
  }

  public int getSubmissionsLimit() {
    return submissionsLimit;
  }

  @NotNull
  public SubmittableFile[] getFiles() {
    return files;
  }

  public boolean isSubmittable() {
    return files.length > 0;
  }
}
