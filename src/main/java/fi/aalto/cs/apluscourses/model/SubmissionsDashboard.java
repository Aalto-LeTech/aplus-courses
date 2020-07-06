package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SubmissionsDashboard {

  private final int studentId;
  private final List<SubmissionResult> submissionResults;
  private final int totalPoints;

  public SubmissionsDashboard(int studentId, List<SubmissionResult> submissionResults,
      int totalPoints) {
    this.studentId = studentId;
    this.submissionResults = submissionResults;
    this.totalPoints = totalPoints;
  }

  @NotNull
  public static SubmissionsDashboard fromJsonObject(@NotNull JSONObject jsonObject) {
    List<SubmissionResult> submissionResults = new ArrayList<>();
    int studentId = jsonObject.getInt("id");
    int points = jsonObject.getInt("points");
    JSONArray modulesArray = jsonObject.getJSONArray("modules");

    for (int i = 0; i < modulesArray.length(); ++i) {
      JSONObject module = modulesArray.getJSONObject(i);
      JSONArray exercisesArray = module.getJSONArray("exercises");
      for (int j = 0; j < exercisesArray.length(); ++j) {
        JSONObject exercise = exercisesArray.getJSONObject(j);
        SubmissionResult submissionResult = SubmissionResult.fromJsonObject(exercise);
        submissionResults.add(submissionResult);
      }
    }

    return new SubmissionsDashboard(studentId, submissionResults, points);
  }

  /**
   * Get the all the submissions for the given course by current user from the A+ API.
   *
   * @throws IOException If an IO error occurs (e.g. network error).
   */
  @NotNull
  public static SubmissionsDashboard getSubmissionsDashboard(long courseId,
      @NotNull APlusAuthentication authentication) throws IOException {
    URL url = getSubmissionsDashboardAPIURL(courseId);
    InputStream inputStream = CoursesClient.fetch(url, authentication::addToRequest);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    return fromJsonObject(response);
  }

  // todo: to propose combining all the calls like this together to test
  public static URL getSubmissionsDashboardAPIURL(long courseId) throws MalformedURLException {
    return new URL(PluginSettings.A_PLUS_API_BASE_URL + "/courses/" + courseId + "/points/me/");
  }
}
