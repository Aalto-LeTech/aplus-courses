package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SubmissionInfo {

  private final int submissionsLimit;

  @NotNull
  private final List<SubmittableFile> files;

  /**
   * Construct a submission info instance with the given submission limit and filenames.
   */
  public SubmissionInfo(int submissionsLimit, @NotNull List<SubmittableFile> files) {
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

    return new SubmissionInfo(submissionLimit, files);
  }

  /**
   * Makes a request to the A+ API to get the details of the given exercise.
   *
   * @throws IOException If an IO error occurs (e.g. network error).
   */
  @NotNull
  public static SubmissionInfo forExercise(@NotNull Exercise exercise,
                                           @NotNull APlusAuthentication authentication)
      throws IOException {
    URL url = new URL(PluginSettings.A_PLUS_API_BASE_URL + "/exercises/" + exercise.getId() + "/");
    InputStream inputStream = CoursesClient.fetch(url, authentication);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    return fromJsonObject(response);
  }

  public int getSubmissionsLimit() {
    return submissionsLimit;
  }

  @NotNull
  public List<SubmittableFile> getFiles() {
    return Collections.unmodifiableList(files);
  }
}
