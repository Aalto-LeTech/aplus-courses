package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleMetadata;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.TestResults;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseFileManager {

  private static final Logger logger = LoggerFactory.getLogger(CourseFileManager.class);

  private File courseFile;
  private URL courseUrl;
  private String language;
  private Map<String, ModuleMetadata> modulesMetadata;
  private Map<Long, TestResults> testResults;

  private static final String COURSE_FILE_NAME = "a-plus-project.json";
  private static final String URL_KEY = "url";
  private static final String LANGUAGE_KEY = "language";
  private static final String EXERCISES_KEY = "exercises";
  private static final String SUBMISSIONS_KEY = "submissions";
  private static final String TEST_RESULTS_KEY = "testResults";
  private static final String MODULES_KEY = "modules";
  private static final String MODULE_ID_KEY = "id";
  private static final String MODULE_DOWNLOADED_AT_KEY = "downloadedAt";

  public CourseFileManager(@NotNull Project project) {
    courseFile = getCourseFile(project);
  }

  /**
   * Attempts to create a course file and load it for the given project with the given URL and
   * language. If a course file for already exists, then the URL and language is overwritten, but
   * the existing modules and exercises metadata in the course file is preserved.
   *
   * @param courseUrl The URL that gets added to the course file.
   * @param language  The language string that gets added to the course file.
   * @throws IOException If an IO error occurs.
   */
  public synchronized void createAndLoad(@NotNull URL courseUrl,
                                         @NotNull String language) throws IOException {
    if (courseFile.exists()) {
      load();
    } else {
      // createAndLoad never overwrites modules metadata of an existing course file.
      // Should this be in the constructor?
      this.modulesMetadata = new HashMap<>();
      this.testResults = new HashMap<>();
    }
    this.courseUrl = courseUrl;
    this.language = language;
    writeCourseFile(
        new JSONObject()
            .put(URL_KEY, courseUrl.toString())
            .put(LANGUAGE_KEY, language)
            .put(MODULES_KEY, createModulesObject())
            .put(EXERCISES_KEY, createExercisesObject())
    );
  }

  /**
   * Attempts to load the course file corresponding to the given project. Returns {@code false} if
   * the course file doesn't exist, {@code true} otherwise.
   *
   * @return {@code true} if the course file was successfully loaded, {@code false} if the course
   *         file doesn't exist.
   * @throws IOException   If an IO error occurs while reading the course file.
   * @throws JSONException If the course file contains malformed JSON.
   */
  public synchronized boolean load() throws IOException {
    if (courseFile.exists()) {
      JSONObject jsonObject = readCourseFile();
      loadFromJsonObject(jsonObject);
      return true;
    }
    return false;
  }

  /**
   * Adds an entry for the given submission result's test results to the
   * currently loaded course file. If an entry already exists for the
   * given test results, then it is overwritten with the new entry.
   *
   * @param result The submission result for which an entry is added.
   * @throws IOException If an IO error occurs while writing to the course file.
   */
  public synchronized void addTestResultsEntry(@NotNull SubmissionResult result)
          throws IOException {
    if (result.getTestResults().isEmpty()) {
      return;
    }

    String exerciseIdString = String.valueOf(result.getExercise().getId());
    HashMap<String, Integer> resultsMap = result.getTestResults();

    JSONObject newResultsObject = new JSONObject()
        .put(TEST_RESULTS_KEY, new JSONObject()
              .put("succeeded", resultsMap.get("succeeded"))
              .put("failed",    resultsMap.get("failed"))
              .put("canceled",  resultsMap.get("canceled"))
    );

    JSONObject exercisesObject = createExercisesObject();
    JSONObject modulesObject = createModulesObject();


    if (!exercisesObject.has(exerciseIdString)) {
      exercisesObject.put(exerciseIdString, new JSONObject());
    }

    if (!exercisesObject.getJSONObject(exerciseIdString).has(SUBMISSIONS_KEY)) {
      exercisesObject.getJSONObject(exerciseIdString)
              .put(SUBMISSIONS_KEY, new JSONObject());
    }

    exercisesObject
            .getJSONObject(exerciseIdString)
            .getJSONObject(SUBMISSIONS_KEY)
            .put(String.valueOf(result.getId()), newResultsObject);

    JSONObject jsonObject = new JSONObject()
        .put(URL_KEY, courseUrl.toString())
        .put(LANGUAGE_KEY, language)
        .put(MODULES_KEY, modulesObject)
        .put(EXERCISES_KEY, exercisesObject);

    writeCourseFile(jsonObject);

    TestResults results = new TestResults(
            resultsMap.get("succeeded"),
            resultsMap.get("failed"),
            resultsMap.get("canceled"),
            result.getExercise().getId(),
            result.getId());

    // Only add the entry to the map after writing to the file, so that if the write fails, the map
    // is still in the correct state.
    testResults.put(result.getId(), results);
  }


  /**
   * Adds an entry for the given module to the currently loaded course file. If an entry already
   * exists for the given module, then it is overwritten with the new entry.
   *
   * @param module The module for which an entry is added.
   * @throws IOException If an IO error occurs while writing to the course file.
   */
  public synchronized void addModuleEntry(@NotNull Module module) throws IOException {
    ModuleMetadata newModuleMetadata = module.getMetadata();
    JSONObject newModuleObject = new JSONObject()
        .put(MODULE_ID_KEY, newModuleMetadata.getModuleId())
        .put(MODULE_DOWNLOADED_AT_KEY, newModuleMetadata.getDownloadedAt());

    JSONObject modulesObject = createModulesObject();
    JSONObject exercisesObject = createExercisesObject();

    modulesObject.put(module.getName(), newModuleObject);

    JSONObject jsonObject = new JSONObject()
        .put(URL_KEY, courseUrl.toString())
        .put(LANGUAGE_KEY, language)
        .put(MODULES_KEY, modulesObject)
        .put(EXERCISES_KEY, exercisesObject);

    writeCourseFile(jsonObject);

    // Only add the entry to the map after writing to the file, so that if the write fails, the map
    // is still in the correct state.
    modulesMetadata.put(module.getName(), newModuleMetadata);
  }

  /**
   * Returns a hashmap containing test results for a given submissionId.
   */
  public synchronized Map<String, Integer> getTestResults(Long submissionId) {
    TestResults result = testResults.get(submissionId);
    return result != null
            ? result.getTestResults()
            : new HashMap<>();
  }

  /**
   * Returns the URL of the course for the currently loaded course file. This should only be called
   * after a course file has been successfully loaded.
   */
  @NotNull
  public synchronized URL getCourseUrl() {
    return courseUrl;
  }

  /**
   * Returns the language chosen by the user for the course corresponding to the currently loaded
   * course file. This should only be called after a course file has been successfully loaded.
   * @return
   */
  @NotNull
  public synchronized String getLanguage() {
    return language;
  }

  /**
   * Returns the metadata of modules in the currently loaded course file, or an empty map if no
   * course file has been loaded.
   */
  @NotNull
  public synchronized Map<String, ModuleMetadata> getModulesMetadata() {
    // Return a copy so that later changes to the map aren't visible in the returned map.
    return modulesMetadata != null ? new HashMap<>(modulesMetadata) : Collections.emptyMap();
  }

  @NotNull
  private JSONObject readCourseFile() throws IOException {
    return new JSONObject(FileUtils.readFileToString(courseFile, StandardCharsets.UTF_8));
  }

  @NotNull
  private void writeCourseFile(@NotNull JSONObject jsonObject) throws IOException {
    FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);
  }

  /*
   * Returns the course file corresponding to the given project.
   */
  @NotNull
  private File getCourseFile(@NotNull Project project) {
    return Paths
        .get(Objects.requireNonNull(project.getBasePath()),
            Project.DIRECTORY_STORE_FOLDER,
            COURSE_FILE_NAME)
        .toFile();
  }

  /*
   * Returns a JSONObject corresponding to the contents of the testResults map.
   */
  @NotNull
  private JSONObject createExercisesObject() {
    JSONObject exercisesObject = new JSONObject();
    HashMap<Long, List<TestResults>> testResultsMap = new HashMap<>();
    testResults.forEach((submissionId, testResults) -> {
      long id = testResults.getExerciseId();
      if (!testResultsMap.containsKey(id)) {
        testResultsMap.put(id, new ArrayList<>());
      }
      testResultsMap.get(id).add(testResults);
    });
    testResultsMap.forEach((exerciseId, testResults) -> {
      JSONObject exerciseObject = new JSONObject()
              .put(SUBMISSIONS_KEY, new JSONObject());
      testResults.forEach(testResult -> {
        HashMap<String, Integer> results = testResult.getTestResults();
        exerciseObject
            .getJSONObject(SUBMISSIONS_KEY)
            .put(testResult.getSubmissionId().toString(), new JSONObject()
            .put(TEST_RESULTS_KEY, new JSONObject()
                .put("succeeded", results.get("succeeded"))
                .put("failed",    results.get("failed"))
                .put("canceled",  results.get("canceled"))
          ));
      });
      exercisesObject.put(exerciseId.toString(), exerciseObject);
    });
    return exercisesObject;
  }


  /*
   * Returns a JSONObject corresponding to the contents of the modulesMetadata map.
   */
  @NotNull
  private JSONObject createModulesObject() {
    JSONObject modulesObject = new JSONObject();
    modulesMetadata.forEach((name, metadata) -> modulesObject
        .put(name, new JSONObject()
            .put(MODULE_ID_KEY, metadata.getModuleId())
            .put(MODULE_DOWNLOADED_AT_KEY, metadata.getDownloadedAt())));
    return modulesObject;
  }

  /*
   * Initializes local variables from the given JSON object.
   */
  private void loadFromJsonObject(@NotNull JSONObject jsonObject) throws IOException {
    this.courseUrl = new URL(jsonObject.getString(URL_KEY));

    try {
      this.language = jsonObject.getString(LANGUAGE_KEY);
    } catch (JSONException e) {
      logger.error("Course file missing language, defaulting to 'en'", e);
      language = "en";
    }

    this.modulesMetadata = new HashMap<>();
    JSONObject modulesObject = jsonObject.optJSONObject(MODULES_KEY);
    if (modulesObject == null) {
      return;
    }

    Iterable<String> moduleNames = modulesObject::keys;
    for (String moduleName : moduleNames) {
      JSONObject moduleObject = modulesObject.getJSONObject(moduleName);

      String moduleId = moduleObject.getString(MODULE_ID_KEY);

      ZonedDateTime downloadedAt;
      try {
        downloadedAt = ZonedDateTime.parse(moduleObject.getString(MODULE_DOWNLOADED_AT_KEY));
      } catch (JSONException e) {
        logger.error("Module " + moduleName + " missing 'downloadedAt' in course file", e);
        downloadedAt = Instant.EPOCH.atZone(ZoneId.systemDefault());
      }

      this.modulesMetadata.put(moduleName, new ModuleMetadata(moduleId, downloadedAt));
    }

    this.testResults = new HashMap<>();
    JSONObject exercisesObject = jsonObject.optJSONObject(EXERCISES_KEY);
    if (exercisesObject == null) {
      return;
    }

    Iterable<String> exerciseIds = exercisesObject::keys;
    for (String exerciseIdString : exerciseIds) {
      long exerciseId = Long.parseLong(exerciseIdString);
      JSONObject exerciseObject = exercisesObject.getJSONObject(exerciseIdString);
      JSONObject submissionsObject = exerciseObject.optJSONObject(SUBMISSIONS_KEY);
      if (submissionsObject == null) {
        continue;
      }
      Iterable<String> submissionIds = submissionsObject::keys;
      for (String submissionIdString : submissionIds) {
        long submissionId = Long.parseLong(submissionIdString);
        JSONObject submissionObject = submissionsObject.getJSONObject(submissionIdString);
        JSONObject resultsObject = submissionObject.getJSONObject(TEST_RESULTS_KEY);
        this.testResults.put(submissionId, new TestResults(
                        resultsObject.getInt("succeeded"),
                        resultsObject.getInt("failed"),
                        resultsObject.getInt("canceled"),
                        exerciseId,
                        submissionId
        ));
      }
    }
  }
}
