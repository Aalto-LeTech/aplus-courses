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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class APlusExerciseDataSource implements ExerciseDataSource {

  @NotNull
  private final Client client;

  @NotNull
  private final String apiUrl;

  @NotNull
  private final Parser parser;

  /**
   * Default constructor.
   */
  public APlusExerciseDataSource(@NotNull String apiUrl) {
    this(apiUrl,
        DefaultDataAccess.INSTANCE,
        DefaultDataAccess.INSTANCE);
  }

  /**
   * Constructor for demanding use (e.g. tests).
   *
   * @param client         Client to fetch and post.
   * @param apiUrl         The base URL of API.
   * @param parser         JSON parser.
   */
  public APlusExerciseDataSource(@NotNull String apiUrl,
                                 @NotNull Client client,
                                 @NotNull Parser parser) {
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
  public SubmissionInfo getSubmissionInfo(@NotNull Exercise exercise,
                                          @NotNull Authentication authentication)
      throws IOException {
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
  public SubmissionHistory getSubmissionHistory(@NotNull Exercise exercise,
                                                @NotNull Authentication authentication)
      throws IOException {
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
  public List<Group> getGroups(@NotNull Course course, @NotNull Authentication authentication)
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
  public List<ExerciseGroup> getExerciseGroups(@NotNull Course course,
                                               @NotNull Points points,
                                               @NotNull Authentication authentication)
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
  public Points getPoints(@NotNull Course course, @NotNull Authentication authentication)
      throws IOException {
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
  public void submit(@NotNull Submission submission, @NotNull Authentication authentication)
      throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("__aplus__", "{ \"group\": " + submission.getGroup().getId() + ", \"lang\": \"en\" }");
    for (Map.Entry<String, Path> entry : submission.getFiles().entrySet()) {
      data.put(entry.getKey(), entry.getValue().toFile());
    }
    String url = apiUrl + "/exercises/" + submission.getExercise().getId() + "/submissions/submit/";
    client.post(url, authentication, data);
  }

  @NotNull
  public Client getClient() {
    return client;
  }

  @NotNull
  public String getApiUrl() {
    return apiUrl;
  }

  @NotNull
  public Parser getParser() {
    return parser;
  }

  public static class DefaultDataAccess implements Client, Parser {

    public static final DefaultDataAccess INSTANCE = new DefaultDataAccess();

    private DefaultDataAccess() {

    }

    @Override
    public JSONObject fetch(@NotNull String url, @Nullable Authentication authentication)
        throws IOException {
      try (InputStream inputStream = CoursesClient.fetch(new URL(url), authentication)) {
        return new JSONObject(new JSONTokener(inputStream));
      }
    }

    @Override
    public void post(@NotNull String url,
                     @NotNull Authentication authentication,
                     @NotNull Map<String, Object> data)
        throws IOException {
      CoursesClient.post(new URL(url), authentication, data);
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
