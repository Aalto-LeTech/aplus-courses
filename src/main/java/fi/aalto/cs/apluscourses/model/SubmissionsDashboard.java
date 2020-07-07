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

  private final long studentId;
  private final List<SubmissionResult> submissionResults;
  private final int totalPoints;

  /**
   * Hi there! It turned out, that there is a way to get all the data on {@link Course} {@link
   * SubmissionResult}s is one call. Well, so here I am, the {@link SubmissionsDashboard} ;-) .
   *
   * @param studentId an A+ student Id.
   * @param submissionResults a {@link List} with {@link SubmissionResult}s having the data.
   * @param totalPoints an amount of points achieved so far for the whole course.
   */
  public SubmissionsDashboard(long studentId,
                              List<SubmissionResult> submissionResults,
                              int totalPoints) {
    this.studentId = studentId;
    this.submissionResults = submissionResults;
    this.totalPoints = totalPoints;
  }

  /**
   * Constructs a {@link SubmissionsDashboard} from the given JSON object. The object must contain an long value for the
   * key "id", integer value for the key "points" and an array value for the key "modules"
   * (containing another array for the key "exercises" in its turn).
   *
   * @param jsonObject The JSON object from which the dashboard is constructed.
   * @return a SubmissionsDashboard instance.
   */
  @NotNull
  public static SubmissionsDashboard fromJsonObject(@NotNull JSONObject jsonObject) {
    List<SubmissionResult> submissionResults = new ArrayList<>();
    long studentId = jsonObject.getLong("id");
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

  /**
   * An API endpoint represented as {@link URL}.
   *
   * @param courseId an id for the course to fetch the results for.
   * @return a {@link URL} representation of the endpoint location.
   * @throws MalformedURLException an exception thrown in case of something went wrong.
   */
  public static URL getSubmissionsDashboardAPIURL(long courseId) throws MalformedURLException {
    return new URL(PluginSettings.A_PLUS_API_BASE_URL + "/courses/" + courseId + "/points/me/");
  }

  public long getStudentId() {
    return studentId;
  }

  public List<SubmissionResult> getSubmissionResults() {
    return submissionResults;
  }

  public int getTotalPoints() {
    return totalPoints;
  }
}
