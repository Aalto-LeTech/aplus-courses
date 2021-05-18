package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Cache;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.JsonCache;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.Tutorial;
import fi.aalto.cs.apluscourses.model.User;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZonedDateTime;
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

public class APlusExerciseDataSource implements ExerciseDataSource {

  private static final String EXERCISES = "exercises";
  private static final String SUBMISSIONS = "submissions";
  private static final String COURSES = "courses";
  private static final String POINTS = "points";
  private static final String USERS = "users";

  @NotNull
  private final Client client;

  @NotNull
  private final String apiUrl;

  @NotNull
  private final Parser parser;


  /**
   * Default constructor.
   */
  public APlusExerciseDataSource(@NotNull String apiUrl, @NotNull Path cacheFile) {
    var dataAccess = new DefaultDataAccess(new JsonCache(cacheFile));
    this.client = dataAccess;
    this.parser = dataAccess;
    this.apiUrl = apiUrl;
  }

  /**
   * Constructor for demanding use (e.g. tests).
   *
   * @param client Client to fetch and post.
   * @param apiUrl The base URL of API.
   * @param parser JSON parser.
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
    String url = apiUrl + EXERCISES + "/" + exercise.getId() + "/";
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
    String url = apiUrl + EXERCISES + "/" + exercise.getId() + "/" + SUBMISSIONS + "/me/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseSubmissionHistory(response);
  }

  /**
   * Get all of the groups from the A+ API for the user corresponding to the given authentication. A
   * group with id 0 and a single member name "Submit alone" is added to the beginning of the list.
   *
   * @return A list of {@link Group}s that the user is a member of in the given course.
   * @throws IOException If an error occurs (e.g. network error).
   */
  @NotNull
  public List<Group> getGroups(@NotNull Course course, @NotNull Authentication authentication)
      throws IOException {
    String url = apiUrl + COURSES + "/" + course.getId() + "/mygroups/";
    JSONObject response = client.fetch(url, authentication);
    return List.of(JsonUtil.parseArray(response.getJSONArray("results"),
        JSONArray::getJSONObject,
        parser::parseGroup,
        Group[]::new));
  }

  /**
   * Get all of the exercise groups in the given course by making a request to the A+ API.
   *
   * @throws IOException If an IO error occurs (for an example a network issue). This is an instance
   *                     of {@link InvalidAuthenticationException} if authentication is invalid.
   */
  @Override
  @NotNull
  public List<ExerciseGroup> getExerciseGroups(@NotNull Course course,
                                               @NotNull Points points,
                                               @NotNull Map<Long, Tutorial> tutorials,
                                               @NotNull Authentication authentication)
      throws IOException {
    String url = apiUrl + COURSES + "/" + course.getId() + "/" + EXERCISES + "/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseExerciseGroups(response.getJSONArray("results"), points, tutorials);
  }

  /**
   * Get all of the points for the given course by making a request to the A+ API.
   *
   * @throws IOException If an IO error occurs (for an example a network issue). This is an instance
   *                     of {@link InvalidAuthenticationException} if authentication is invalid.
   */
  @Override
  @NotNull
  public Points getPoints(@NotNull Course course, @NotNull Authentication authentication)
      throws IOException {
    String url = apiUrl + COURSES + "/" + course.getId() + "/" + POINTS + "/me/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parsePoints(response);
  }

  @Override
  @NotNull
  public SubmissionResult getSubmissionResult(@NotNull String submissionUrl,
                                              @NotNull Exercise exercise,
                                              @NotNull Authentication authentication,
                                              @NotNull ZonedDateTime minCacheEntryTime)
      throws IOException {
    JSONObject response = client.fetch(submissionUrl, authentication, minCacheEntryTime);
    return parser.parseSubmissionResult(response, exercise);
  }

  @Override
  @NotNull
  public User getUser(@NotNull Authentication authentication) throws IOException {
    String url = apiUrl + USERS + "/me/";
    JSONObject response = client.fetch(url, authentication);
    return new User(authentication, parser.parseUserName(response));
  }

  /**
   * Sends the submission to the server.
   *
   * @throws IOException If there are IO related errors.
   */
  @Override
  @Nullable
  public String submit(@NotNull Submission submission, @NotNull Authentication authentication)
      throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("__aplus__", "{ \"group\": " + submission.getGroup().getId() + ", \"lang\": \""
        + submission.getLanguage() + "\" }");
    for (Map.Entry<String, Path> entry : submission.getFiles().entrySet()) {
      data.put(entry.getKey(), entry.getValue().toFile());
    }
    String url = apiUrl + EXERCISES + "/" + submission.getExercise().getId()
        + "/" + SUBMISSIONS + "/submit/";
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

  @NotNull
  public Parser getParser() {
    return parser;
  }

  public static class DefaultDataAccess implements Client, Parser {

    @NotNull
    private final Cache<String, JSONObject> cache;

    public DefaultDataAccess(@NotNull Cache<String, JSONObject> cache) {
      this.cache = cache;
    }

    @Override
    public JSONObject fetch(@NotNull String url,
                            @Nullable Authentication authentication,
                            @NotNull ZonedDateTime minCacheEntryTime) throws IOException {
      var cacheEntry = cache.getEntry(url);
      if (cacheEntry != null && cacheEntry.getCreationTime().compareTo(minCacheEntryTime) >= 0) {
        return cacheEntry.getValue();
      }
      try (InputStream inputStream = CoursesClient.fetch(new URL(url), authentication)) {
        var response = new JSONObject(new JSONTokener(inputStream));
        cache.putValue(url, response);
        return response;
      }
    }

    @Override
    @Nullable
    public String post(@NotNull String url,
                       @NotNull Authentication authentication,
                       @NotNull Map<String, Object> data) throws IOException {
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
    public SubmissionInfo parseSubmissionInfo(@NotNull JSONObject object) {
      return SubmissionInfo.fromJsonObject(object);
    }

    @Override
    public SubmissionHistory parseSubmissionHistory(@NotNull JSONObject object) {
      return SubmissionHistory.fromJsonObject(object);
    }

    @Override
    public Group parseGroup(@NotNull JSONObject object) {
      return Group.fromJsonObject(object);
    }

    @Override
    public List<ExerciseGroup> parseExerciseGroups(@NotNull JSONArray array,
                                                   @NotNull Points points,
                                                   @NotNull Map<Long, Tutorial> tutorials) {
      return ExerciseGroup.fromJsonArray(array, points, tutorials);
    }

    @Override
    public Points parsePoints(@NotNull JSONObject object) {
      return Points.fromJsonObject(object);
    }

    @Override
    public SubmissionResult parseSubmissionResult(@NotNull JSONObject object,
                                                  @NotNull Exercise exercise) {
      return SubmissionResult.fromJsonObject(object, exercise);
    }

    @Override
    public String parseUserName(@NotNull JSONObject object) {
      var fullName = object.optString("full_name");
      var username = object.optString("username");
      return fullName.equals("") ? username : fullName;
    }
  }
}
