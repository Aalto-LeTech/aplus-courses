package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SubmissionHistory {

  private int numberOfSubmissions;

  public SubmissionHistory(int numberOfSubmissions) {
    this.numberOfSubmissions = numberOfSubmissions;
  }

  @NotNull
  public static SubmissionHistory fromJsonObject(@NotNull JSONObject jsonObject) {
    int count = jsonObject.getInt("count");
    return new SubmissionHistory(count);
  }

  /**
   * Get the submission history for the given exercise from the A+ API.
   *
   * @throws IOException If an IO error occurs (e.g. network error).
   */
  @NotNull
  public static SubmissionHistory getSubmissionHistory(
      long exerciseId, @NotNull APlusAuthentication authentication) throws IOException {
    URL url = new URL(PluginSettings.A_PLUS_API_BASE_URL + "/exercises/" + exerciseId
        + "/submissions/me/");
    InputStream inputStream = CoursesClient.fetch(url, authentication);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    return fromJsonObject(response);
  }

  public int getNumberOfSubmissions() {
    return numberOfSubmissions;
  }

}
