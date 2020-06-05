package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModuleMetadata;
import fi.aalto.cs.apluscourses.model.Module;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class CourseFileManager {

  private File courseFile;
  private URL courseUrl;
  private Map<String, IntelliJModuleMetadata> modulesMetadata;

  private CourseFileManager() {

  }

  private static final CourseFileManager instance = new CourseFileManager();

  private static final String COURSE_FILE_NAME = "a-plus-project.json";

  public static CourseFileManager getInstance() {
    return instance;
  }

  /**
   * Attempts to create a course file and load it for the given project with the given URL. If a
   * course file for already exists (even with a different URL), this method loads the existing
   * course file.
   * @param project      The project for which the course file is created.
   * @param courseUrl    The URL that gets added to the course file (if it doesn't exist yet).
   * @throws IOException If an IO error occurs.
   */
  public synchronized void createAndLoad(@NotNull Project project, @NotNull URL courseUrl)
      throws IOException {
    courseFile = getCourseFile(project);
    if (courseFile.exists()) {
      // If the course file already exists, then this is equivalent to a load
      load(project);
      return;
    }
    this.courseUrl = courseUrl;
    this.modulesMetadata = new HashMap<>();
    writeCourseFile(new JSONObject().put("url", courseUrl.toString()));
  }

  /**
   * Attempts to load the course file corresponding to the given project. Returns {@code false} if
   * the course file doesn't exist, {@code true} otherwise.
   * @param project The project from which the course file is loaded.
   * @return {@code true} if the course file was successfully loaded, {@code false} if the course
   *         file doesn't exist.
   * @throws IOException   If an IO error occurs while reading the course file.
   * @throws JSONException If the course file contains malformed JSON.
   */
  public synchronized boolean load(@NotNull Project project) throws IOException {
    courseFile = getCourseFile(project);
    if (courseFile.exists()) {
      JSONObject jsonObject = readCourseFile();
      loadFromJsonObject(jsonObject);
      return true;
    }
    return false;
  }

  private static final String MODULE_ID_KEY = "id";
  private static final String MODULE_DOWNLOADED_AT_KEY = "downloadedAt";

  /**
   * Adds an entry for the given module to the currently loaded course file. If an entry already
   * exists for the given module, then it is overwritten with the new entry.
   * @param module The module for which an entry is added.
   * @throws IOException If an IO error occurs while writing to the course file.
   */
  public synchronized void addEntryForModule(@NotNull Module module) throws IOException {
    IntelliJModuleMetadata newModuleMetadata
        = new IntelliJModuleMetadata(module.getVersionId(), ZonedDateTime.now());
    JSONObject newModuleObject = new JSONObject()
        .put(MODULE_ID_KEY, newModuleMetadata.getModuleId())
        .put(MODULE_DOWNLOADED_AT_KEY, newModuleMetadata.getDownloadedAt());

    JSONObject modulesObject = createModulesObject();

    modulesObject.put(module.getName(), newModuleObject);

    JSONObject jsonObject = new JSONObject()
        .put("url", courseUrl.toString())
        .put("modules", modulesObject);

    writeCourseFile(jsonObject);

    // Only add the entry to the map after writing to the file, so that if the write fails, the map
    // is still in the correct state.
    modulesMetadata.put(module.getName(), newModuleMetadata);
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
   * Returns the metadata of modules in the currently loaded course file. This should only be called
   * after a course file has been successfully loaded.
   */
  @NotNull
  public synchronized Map<String, IntelliJModuleMetadata> getModulesMetadata() {
    // Return a copy so that later changes to the map aren't visible in the returned map.
    return new HashMap<>(modulesMetadata);
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
  private File getCourseFile(@NotNull Project project) {
    return Paths
        .get(Objects.requireNonNull(project.getBasePath()),
            Project.DIRECTORY_STORE_FOLDER,
            COURSE_FILE_NAME)
        .toFile();
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
    this.courseUrl = new URL(jsonObject.getString("url"));

    this.modulesMetadata = new HashMap<>();
    JSONObject modulesObject = jsonObject.optJSONObject("modules");
    if (modulesObject == null) {
      return;
    }

    Iterable<String> moduleNames = modulesObject::keys;
    for (String moduleName : moduleNames) {
      JSONObject moduleObject = modulesObject.getJSONObject(moduleName);
      String moduleId = moduleObject.getString(MODULE_ID_KEY);
      ZonedDateTime downloadedAt
          = ZonedDateTime.parse(moduleObject.getString(MODULE_DOWNLOADED_AT_KEY));
      this.modulesMetadata.put(moduleName, new IntelliJModuleMetadata(moduleId, downloadedAt));
    }
  }

}
