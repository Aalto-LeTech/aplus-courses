package fi.aalto.cs.apluscourses.dal;

import static fi.aalto.cs.apluscourses.utils.ListUtil.appendTwoLists;

import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.exercise.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.exercise.Points;
import fi.aalto.cs.apluscourses.model.Student;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.exercise.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult;
import fi.aalto.cs.apluscourses.model.User;
import fi.aalto.cs.apluscourses.model.news.NewsItem;
import fi.aalto.cs.apluscourses.utils.cache.Cache;
import fi.aalto.cs.apluscourses.utils.cache.CachePreference;
import fi.aalto.cs.apluscourses.utils.cache.CachePreferences;
import fi.aalto.cs.apluscourses.utils.cache.DualCache;
import fi.aalto.cs.apluscourses.utils.cache.JsonFileCache;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class APlusExerciseDataSource implements ExerciseDataSource {

  private static final String EXERCISES = "exercises";
  private static final String SUBMISSIONS = "submissions";
  private static final String COURSES = "courses";
  private static final String POINTS = "points";
  private static final String USERS = "users";
  private static final String STUDENTS = "students";
  public static final String RESULTS = "results";
  public static final String ME = "me";
  private static final String NEWS = "news";

  @NotNull
  private final Client client;

  @NotNull
  private final String apiUrl;

  @NotNull
  private final Parser parser;


  /**
   * Default constructor.
   */
  public APlusExerciseDataSource(@NotNull String apiUrl, @NotNull Path cacheFile, long courseLastModified) {
    var dataAccess = new DefaultDataAccess(new DualCache<>(new JsonFileCache(cacheFile)));
    dataAccess.updateCacheExpiration(courseLastModified);
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

  private <T> List<T> getPaginatedResults(@NotNull String url,
                                          @NotNull Authentication authentication,
                                          @NotNull CachePreference cachePreference,
                                          @NotNull Function<JSONObject, T> parseFunction)
      throws IOException {
    JSONObject response = client.fetch(url, authentication, cachePreference);
    var results = parser.parsePaginatedResults(response, parseFunction);
    var nextPage = parser.parseNextPageUrl(response);

    return nextPage == null ? results
        : appendTwoLists(results, getPaginatedResults(nextPage, authentication, cachePreference, parseFunction));
  }

  private <T> List<T> getPaginatedResults(@NotNull String url,
                                          @NotNull Authentication authentication,
                                          @NotNull Function<JSONObject, T> parseFunction)
      throws IOException {
    return getPaginatedResults(url, authentication, CachePreferences.GET_NEW_AND_FORGET, parseFunction);
  }

  @NotNull
  private Map<Long, List<Long>> getExerciseOrder(@NotNull Course course, @NotNull Authentication authentication)
      throws IOException {
    String url = apiUrl + COURSES + "/" + course.id + "/tree/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseExerciseOrder(response);
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
    String url = apiUrl + COURSES + "/" + course.id + "/mygroups/";
    return getPaginatedResults(url, authentication, Group::fromJsonObject);
  }

  @Override
  public void clearCache() {
    client.clearCache();
  }

  @Override
  public void updateCacheExpiration(long courseLastModified) {
    client.updateCacheExpiration(courseLastModified);
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
                                               @NotNull Authentication authentication,
                                               @NotNull String languageCode)
      throws IOException {
    return Collections.emptyList();
//    String url = apiUrl + COURSES + "/" + course.getId() + "/" + EXERCISES + "/";
//    var exerciseOrder = getExerciseOrder(course, authentication);
//    return getPaginatedResults(url, authentication,
//        object -> ExerciseGroup.Companion.fromJsonObject(object, exerciseOrder, languageCode,
//            course.getHiddenElements()));
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
    return getPoints(course, authentication, null);
  }

  @Override
  @NotNull
  public Points getPoints(@NotNull Course course, @NotNull Authentication authentication,
                          @Nullable Student student) throws IOException {
    String url = apiUrl + COURSES + "/" + course.id + "/" + POINTS + "/"
        + (student == null ? ME : student.getId()) + "/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parsePoints(response);
  }

  @Override
  @NotNull
  public SubmissionResult getSubmissionResult(@NotNull String submissionUrl,
                                              @NotNull Exercise exercise,
                                              @NotNull Authentication authentication,
                                              @NotNull Course course,
                                              @NotNull CachePreference cachePreference)
      throws IOException {
    JSONObject response = client.fetch(submissionUrl, authentication, cachePreference);
    return parser.parseSubmissionResult(response, exercise, course);
  }

  @Override
  @NotNull
  public Exercise getExercise(long exerciseId,
                              @NotNull Points points,
                              @NotNull Set<String> optionalCategories,
                              @NotNull Authentication authentication,
                              @NotNull CachePreference cachePreference,
                              @NotNull String languageCode) throws IOException {
    var url = apiUrl + "exercises/" + exerciseId + "/";
    var response = client.fetch(url, authentication, cachePreference);
    return parser.parseExercise(response, points, optionalCategories, languageCode);
  }

  @Override
  @NotNull
  public String getSubmissionFeedback(long submissionId,
                                      @NotNull Authentication authentication) throws IOException {
    var url = apiUrl + "submissions/" + submissionId + "/";
    var response = client.fetch(url, authentication, CachePreferences.GET_NEW_AND_FORGET);
    return response.getString("feedback");
  }

  @Override
  @NotNull
  public User getUser(@NotNull Authentication authentication) throws IOException {
    String url = apiUrl + USERS + "/" + ME + "/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseUser(authentication, response);
  }

  @Override
  @NotNull
  public List<Student> getStudents(@NotNull Course course,
                                   @NotNull Authentication authentication,
                                   @NotNull CachePreference cachePreference) throws IOException {
    String url = apiUrl + COURSES + "/" + course.id + "/" + STUDENTS + "/";
    return getPaginatedResults(url, authentication, cachePreference, Student::fromJsonObject);
  }

  @Override
  @NotNull
  public ZonedDateTime getEndingTime(@NotNull Course course,
                                     @NotNull Authentication authentication)
      throws IOException {
    String url = apiUrl + COURSES + "/" + course.id + "/";
    JSONObject response = client.fetch(url, authentication);
    return parser.parseEndingTime(response);
  }

  @Override
  public @NotNull List<NewsItem> getNews(@NotNull Course course,
                                         @NotNull Authentication authentication,
                                         @NotNull String language) throws IOException {
    String url = apiUrl + COURSES + "/" + course.id + "/" + NEWS + "/";
    return getPaginatedResults(url, authentication, CachePreferences.GET_NEW_AND_FORGET,
        jsonObject -> NewsItem.fromJsonObject(jsonObject, course, language));
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
                            @NotNull CachePreference cachePreference) throws IOException {
      var value = cache.getValue(url, cachePreference);
      if (value != null) {
        return value;
      }
//      try (InputStream inputStream = CoursesClient.fetch(new URL(url), authentication)) {
//        var streamString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
//        var response = new JSONObject(new JSONTokener(streamString));
//        cache.putValue(url, response, cachePreference);
//        return response;
//      }
      return value;
    }

    @Override
    @Nullable
    public String post(@NotNull String url,
                       @NotNull Authentication authentication,
                       @NotNull Map<String, Object> data) throws IOException {
//      return CoursesClient.post(
//          new URL(url),
//          authentication,
//          data,
//          response -> Optional
//              .ofNullable(response.getFirstHeader("Location"))
//              .map(Header::getValue)
//              .orElse(null)
//      );
      return null;
    }

    @Override
    public <T> List<T> parsePaginatedResults(@NotNull JSONObject object,
                                             @NotNull Function<JSONObject, T> parseFunction) {
      var jsonResults = object.getJSONArray(RESULTS);
      var results = new ArrayList<T>();
      for (int i = 0; i < jsonResults.length(); i++) {
        results.add(parseFunction.apply(jsonResults.getJSONObject(i)));
      }
      return results;
    }

    @Override
    public String parseNextPageUrl(@NotNull JSONObject object) {
      return object.optString("next", null);
    }

    @Override
    public SubmissionInfo parseSubmissionInfo(@NotNull JSONObject object) {
      return null;
    }

//    @Override
//    public SubmissionInfo parseSubmissionInfo(@NotNull JSONObject object) {
//      return SubmissionInfo.fromJsonObject(object);
//    }

    @Override
    public Group parseGroup(@NotNull JSONObject object) {
      return Group.fromJsonObject(object);
    }

    @Override
    public Points parsePoints(@NotNull JSONObject object) {
      return null;
    }

//    @Override
//    public Points parsePoints(@NotNull JSONObject object) {
//      return Points.fromJsonObject(object);
//    }

    @Override
    public Map<Long, List<Long>> parseExerciseOrder(@NotNull JSONObject object) {
      var modules = object.getJSONArray("modules");
      var exerciseIds = new HashMap<Long, List<Long>>();

      for (int i = 0; i < modules.length(); i++) {
        var module = modules.getJSONObject(i);
        var moduleChildren = module.getJSONArray("children");
        var moduleExerciseIds = new ArrayList<Long>();

        for (int j = 0; j < moduleChildren.length(); j++) {
          var chapterChildren = moduleChildren.getJSONObject(j).getJSONArray("children");
          for (int k = 0; k < chapterChildren.length(); k++) {
            moduleExerciseIds.add(chapterChildren.getJSONObject(k).getLong("id"));
          }
        }

        var moduleId = module.getLong("id");
        exerciseIds.put(moduleId, moduleExerciseIds);
      }
      return exerciseIds;
    }

    @Override
    public Exercise parseExercise(@NotNull JSONObject jsonObject, @NotNull Points points,
                                  @NotNull Set<String> optionalCategories, @NotNull String languageCode) {
      return null;
    }

    @Override
    public SubmissionResult parseSubmissionResult(@NotNull JSONObject jsonObject, @NotNull Exercise exercise,
                                                  @NotNull Course course) {
      return null;
    }

//    @Override
//    public Exercise parseExercise(@NotNull JSONObject jsonObject,
//                                  @NotNull Points points,
//                                  @NotNull Set<String> optionalCategories,
//                                  @NotNull String languageCode) {
//      return Exercise.Companion.fromJsonObject(jsonObject, points, optionalCategories, languageCode);
//    }

//    @Override
//    public SubmissionResult parseSubmissionResult(@NotNull JSONObject object,
//                                                  @NotNull Exercise exercise,
//                                                  @NotNull Course course) {
//      return SubmissionResult.fromJsonObject(object, exercise, course);
//    }

    @Override
    public User parseUser(@NotNull Authentication authentication,
                          @NotNull JSONObject object) {
      var fullName = object.optString("full_name");
      var username = object.optString("username");
      var studentId = object.optString("student_id");
      var id = object.optInt("id");

      return new User(authentication, fullName.equals("") ? username : fullName, studentId, id);
    }

    @Override
    public ZonedDateTime parseEndingTime(@NotNull JSONObject object) {
      return ZonedDateTime.parse(object.getString("ending_time"));
    }

    @Override
    public void clearCache() {
      cache.clearAll();
    }

    @Override
    public void updateCacheExpiration(long courseLastModified) {
      cache.updateExpirationTimestamp(courseLastModified);
    }
  }
}
