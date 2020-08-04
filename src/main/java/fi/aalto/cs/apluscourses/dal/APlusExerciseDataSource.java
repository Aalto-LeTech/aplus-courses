package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.Header;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class APlusExerciseDataSource extends ExerciseDataSource {

  @NotNull
  private final Client client;

  @NotNull
  private String apiUrl;

  @NotNull
  private final Parser parser;

  /**
   * Default constructor.
   *
   * @param authProvider Used for creating an authentication for this data source.
   */
  public APlusExerciseDataSource(@NotNull AuthProvider authProvider) {
    this(authProvider,
        DefaultDataAccess.INSTANCE,
        PluginSettings.A_PLUS_API_BASE_URL,
        DefaultDataAccess.INSTANCE);
  }

  /**
   * Constructor for demanding use (e.g. tests).
   *
   * @param authProvider Authentication provider.
   * @param client       Client to fetch and post.
   * @param apiUrl       The base URL of API.
   * @param parser       JSON parser.
   */
  public APlusExerciseDataSource(@NotNull AuthProvider authProvider,
                                 @NotNull Client client,
                                 @NotNull String apiUrl,
                                 @NotNull Parser parser) {
    super(authProvider);
    this.client = client;
    this.apiUrl = apiUrl;
    this.parser = parser;
  }

  /**
   * Makes a request to the A+ API to get the details of the given exercise.
   *
   * @throws IOException If an IO error occurs (e.g. network error).
   */
  @Override
  @NotNull
  public SubmissionInfo getSubmissionInfo(@NotNull Exercise exercise) throws IOException {
    String url = apiUrl + "/exercises/" + exercise.getId() + "/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseSubmissionInfo(response);
  }

  /**
   * Get the submission history for the given exercise from the A+ API.
   *
   * @throws IOException If an IO error occurs (e.g. network error).
   */
  @Override
  @NotNull
  public SubmissionHistory getSubmissionHistory(@NotNull Exercise exercise) throws IOException {
    String url = apiUrl + "/exercises/" + exercise.getId() + "/submissions/me/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseSubmissionHistory(response);
  }

  /**
   * Get all of the groups from the A+ API for the user corresponding to the given authentication.
   * A group with id 0 and a single member name "Submit alone" is added to the beginning of the
   * list.
   *
   * @return A list of {@link Group}s that the user is a member of in the given course.
   * @throws IOException If an error occurs (e.g. network error).
   */
  @NotNull
  public List<Group> getGroups(@NotNull Course course)
      throws IOException {
    String url = apiUrl + "/courses/" + course.getId() + "/mygroups/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseArray(response.getJSONArray("results"), parser::parseGroup);
  }

  /**
   * Get all of the exercise groups in the given course by making a request to the A+ API.
   * @throws IOException If an IO error occurs (for an example a network issue). This is an instance
   *                     of {@link InvalidAuthenticationException} if authentication is invalid.
   */
  @Override
  @NotNull
  public List<ExerciseGroup> getExerciseGroups(@NotNull Course course, @NotNull Points points)
      throws IOException {
    String url = apiUrl + "/courses/" + course.getId() + "/exercises/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseExerciseGroups(response.getJSONArray("results"), points);
  }

  /**
   * Get all of the points for the given course by making a request to the A+ API.
   * @throws IOException If an IO error occurs (for an example a network issue). This is an instance
   *                     of {@link InvalidAuthenticationException} if authentication is invalid.
   */
  @Override
  @NotNull
  public Points getPoints(@NotNull Course course) throws IOException {
    String url = apiUrl + "/courses/" + course.getId() + "/points/me/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parsePoints(response);
  }

  /**
   * Sends the submission to the server.
   *
   * @throws IOException If there are IO related errors.
   */
  @Override
  @Nullable
  public String submit(Submission submission) throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("__aplus__", "{ \"group\": " + submission.getGroup().getId() + ", \"lang\": \"en\" }");
    for (Map.Entry<String, Path> entry : submission.getFiles().entrySet()) {
      data.put(entry.getKey(), entry.getValue().toFile());
    }
    String url = apiUrl + "/exercises/" + submission.getExercise().getId() + "/submissions/submit/";
    return client.post(url, authentication, data);
  }

  @NotNull
  public Client getClient() {
    return client;
  }

  @NotNull
  public String getApiUrl() {
    return apiUrl;
  }

  public Parser getParser() {
    return parser;
  }

  public static class DefaultDataAccess implements Client, Parser {

    public static final DefaultDataAccess INSTANCE = new DefaultDataAccess();

    private DefaultDataAccess() {

    }

    @Override
    public JSONObject fetch(String url, Authentication authentication) throws IOException {
      try (InputStream inputStream = CoursesClient.fetch(new URL(url), authentication)) {
        return new JSONObject(new JSONTokener(inputStream));
      }
    }

    @Override
    @Nullable
    public String post(String url, Authentication authentication, Map<String, Object> data)
        throws IOException {
      return CoursesClient.post(
          new URL(url),
          authentication,
          data,
          response -> Optional
              .ofNullable(response.getFirstHeader("Location"))
              .map(Header::getValue)
              .orElse(null)
      );
    }

    @Override
    public SubmissionInfo parseSubmissionInfo(JSONObject object) {
      return SubmissionInfo.fromJsonObject(object);
    }

    @Override
    public SubmissionHistory parseSubmissionHistory(JSONObject object) {
      return SubmissionHistory.fromJsonObject(object);
    }

    @Override
    public Group parseGroup(JSONObject object) {
      return Group.fromJsonObject(object);
    }

    @Override
    public List<ExerciseGroup> parseExerciseGroups(JSONArray array, Points points) {
      return ExerciseGroup.fromJsonArray(array, points);
    }

    @Override
    public Points parsePoints(JSONObject object) {
      return Points.fromJsonObject(object);
    }

  }
}
