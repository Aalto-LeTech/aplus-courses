package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class APlusExerciseDataSource implements ExerciseDataSource {

  @NotNull
  private final Authentication authentication = new APlusAuthentication(40);

  @NotNull
  public Authentication getAuthentication() {
    return authentication;
  }

  /**
   * Makes a request to the A+ API to get the details of the given exercise.
   *
   * @throws IOException If an IO error occurs (e.g. network error).
   */
  @Override
  @NotNull
  public SubmissionInfo getSubmissionInfo(@NotNull Exercise exercise) throws IOException {
    URL url = new URL(PluginSettings.A_PLUS_API_BASE_URL + "/exercises/" + exercise.getId() + "/");
    InputStream inputStream = CoursesClient.fetch(url, authentication);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    return SubmissionInfo.fromJsonObject(response);
  }

  /**
   * Get the submission history for the given exercise from the A+ API.
   *
   * @throws IOException If an IO error occurs (e.g. network error).
   */
  @NotNull
  @Override
  public SubmissionHistory getSubmissionHistory(@NotNull Exercise exercise) throws IOException {
    URL url = new URL(PluginSettings.A_PLUS_API_BASE_URL + "/exercises/" + exercise.getId()
        + "/submissions/me/");
    InputStream inputStream = CoursesClient.fetch(url, authentication);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    return SubmissionHistory.fromJsonObject(response);
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
    URL url =
        new URL(PluginSettings.A_PLUS_API_BASE_URL + "/courses/" + course.getId() + "/mygroups/");
    InputStream inputStream = CoursesClient.fetch(url, authentication);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    JSONArray results = response.getJSONArray("results");
    List<Group> groups = new ArrayList<>(results.length() + 1);
    groups.add(new Group(0, Collections.singletonList("Submit alone")));
    for (int i = 0; i < results.length(); ++i) {
      groups.add(Group.fromJsonObject(results.getJSONObject(i)));
    }
    return groups;
  }

  /**
   * Get all of the exercise groups in for the given course by making a request to the A+ API.
   * @throws IOException If an IO error occurs (for an example a network issue). This is an instance
   *                     of {@link InvalidAuthenticationException} if authentication is invalid.
   */
  @NotNull
  public List<ExerciseGroup> getExerciseGroups(@NotNull Course course)
      throws IOException {
    URL url = new URL(PluginSettings.A_PLUS_API_BASE_URL + "/courses/" + course.getId()
        + "/exercises/");
    InputStream inputStream = CoursesClient.fetch(url, authentication);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    JSONArray results = response.getJSONArray("results");
    return ExerciseGroup.fromJsonArray(results);
  }

  /**
   * Sends the submission to the server.
   *
   * @throws IOException If there are IO related errors.
   */
  public void submit(Submission submission) throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("_aplus_group", submission.getGroup().getId());
    for (Path path : submission.getFilePaths()) {
      data.put(path.getFileName().toString(), path.toFile());
    }
    CoursesClient.post(new URL(PluginSettings.A_PLUS_API_BASE_URL + "/exercises/"
        + submission.getExercise().getId()
        + "/submissions/submit/"), authentication, data);
  }
}
