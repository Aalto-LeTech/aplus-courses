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

public class SubmittableExercise extends Exercise {

  private int submissionsLimit;

  @NotNull
  private final List<SubmittableFile> files;

  /**
   * Construct a submittable exercise instance with the given id, name, submission limit,
   * and filenames.
   */
  public SubmittableExercise(long id,
                             @NotNull String name,
                             int submissionsLimit,
                             @NotNull List<SubmittableFile> files) {
    super(id, name);
    this.submissionsLimit = submissionsLimit;
    this.files = files;
  }

  /**
   * Construct a submittable exercise from the given JSON object.
   */
  @NotNull
  public static SubmittableExercise fromJsonObject(@NotNull JSONObject jsonObject) {
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

    long id = jsonObject.getLong("id");
    String name = jsonObject.getString("name");
    int submissionLimit = jsonObject.getInt("max_submissions");

    return new SubmittableExercise(id, name, submissionLimit, files);
  }

  /**
   * Makes a request to the A+ API to get the details of the given exercise.
   *
   * @throws IOException If an IO error occurs (e.g. network error).
   */
  @NotNull
  public static SubmittableExercise fromExerciseId(long exerciseId,
                                                   @NotNull APlusAuthentication authentication)
      throws IOException {
    URL url = new URL(PluginSettings.A_PLUS_API_BASE_URL + "/exercises/" + exerciseId + "/");
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

  /**
   * Returns file paths of the files in this exercise.
   *
   * @param basePath The path inside which the file should be.
   * @return Array of paths
   * @throws FileDoesNotExistException If file not found.
   */
  public Path[] getFilePaths(Path basePath) throws FileDoesNotExistException {
    Path[] paths = new Path[files.size()];
    for (int i = 0; i < files.size(); ++i) {
      paths[i] = files.get(i).getPath(basePath);
    }
    return paths;
  }
}
