package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModuleMetadata;
import fi.aalto.cs.apluscourses.model.Module;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
    courseFile = Paths
        .get(project.getBasePath(), Project.DIRECTORY_STORE_FOLDER, COURSE_FILE_NAME)
        .toFile();
    if (!courseFile.exists()) {
      this.courseUrl = courseUrl;
      this.modulesMetadata = new HashMap<>();
      JSONObject jsonObject = new JSONObject().put("url", courseUrl.toString());
      FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);
    } else {
      internalLoad();
    }
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
    courseFile = Paths
        .get(project.getBasePath(), Project.DIRECTORY_STORE_FOLDER, COURSE_FILE_NAME)
        .toFile();
    if (!courseFile.isFile()) {
      return false;
    } else {
      internalLoad();
      return true;
    }
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
    JSONObject newModuleObject = new JSONObject();
    newModuleObject.put(MODULE_ID_KEY, newModuleMetadata.getModuleId());
    newModuleObject.put(MODULE_DOWNLOADED_AT_KEY, newModuleMetadata.getDownloadedAt());

    JSONObject modulesObject = new JSONObject();
    modulesMetadata.forEach((name, metadata) -> {
      JSONObject moduleObject = new JSONObject();
      moduleObject.put(MODULE_ID_KEY, metadata.getModuleId());
      moduleObject.put(MODULE_DOWNLOADED_AT_KEY, metadata.getDownloadedAt());
      modulesObject.put(name, moduleObject);
    });

    modulesObject.put(module.getName(), newModuleObject);

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("url", courseUrl.toString());
    jsonObject.put("modules", modulesObject);

    FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);

    // Only add the entry to the map after writing to the file, so that if the write fails, the map
    // is still in the correct state.
    modulesMetadata.put(module.getName(), newModuleMetadata);
  }

  /**
   * Returns the URL of the course for the currently loaded course file. This should only be called
   * after a course file has been loaded.
   */
  @NotNull
  public synchronized URL getCourseUrl() {
    return courseUrl;
  }

  /**
   * Returns the metadata of modules in the currently loaded course file. This should only be called
   * after a course file has been loaded.
   */
  @NotNull
  public synchronized Map<String, IntelliJModuleMetadata> getModulesMetadata() {
    // Return a copy so that later changes to the map aren't visible in the returned map.
    return new HashMap<>(modulesMetadata);
  }

  private void internalLoad() throws IOException {
    JSONTokener tokenizer = new JSONTokener(new FileInputStream(courseFile));
    JSONObject jsonObject = new JSONObject(tokenizer);
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
