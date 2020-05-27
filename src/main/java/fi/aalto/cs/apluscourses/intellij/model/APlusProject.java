package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.messages.MessageBus;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Module;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class APlusProject {
  @NotNull
  private final Project project;

  public APlusProject(@NotNull Project project) {
    this.project = project;
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  @NotNull
  public Path getBasePath() {
    return Paths.get(Objects.requireNonNull(project.getBasePath()));
  }

  @NotNull
  public Path getCourseFilePath() {
    return getBasePath().resolve(Paths.get(Project.DIRECTORY_STORE_FOLDER, "a-plus-project.json"));
  }

  /**
   * Creates a local course file containing the given URL. If a course file already exists (even if
   * it was created with a different URL), this method does nothing and returns false.
   * @param sourceUrl The URL that is added to the course file.
   * @return True if the course file was created successfully, false if a course file already
   *         exists.
   * @throws IOException If an IO error occurs while creating the file.
   */
  public boolean createCourseFile(@NotNull URL sourceUrl) throws IOException {
    File courseFile = getCourseFilePath().toFile();
    if (courseFile.isFile()) {
      return false;
    }

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("url", sourceUrl.toString());
    FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);
    return true;
  }

  /**
   * Parses the stored URL from the course file and returns it, or null if the course file doesn't
   * exist.
   * @return The URL in the course file, or null if the course file doesn't exist.
   * @throws IOException   If an IO error occurs while reading the course file.
   * @throws JSONException If the JSON in the course file is malformed.
   */
  @Nullable
  public URL getCourseFileUrl() throws IOException {
    File courseFile = getCourseFilePath().toFile();
    if (!courseFile.isFile()) {
      return null;
    }

    // TODO: the course file is written as UTF-8, ensure that this always works
    synchronized (courseFileLock) {
      JSONTokener tokenizer = new JSONTokener(new FileInputStream(courseFile));
      JSONObject jsonObject = new JSONObject(tokenizer);
      return new URL(jsonObject.getString("url"));
    }
  }

  private static final Object courseFileLock = new Object();

  /**
   * Adds an entry for the given module to the given course file. This method should only be used
   * if when the default course file shouldn't be used (e.g. in testing). Prefer to use {@link
   * APlusProject#addCourseFileEntry(Module)}.
   * @param courseFile The file to which the entry is added(must already contain a valid JSON
   *                   object).
   * @param module     The module for which an entry is added.
   * @throws IOException   If an IO error occurs (for an example, the file doesn't exist).
   * @throws JSONException If the existing JSON in the course file is malformed.
   */
  public void addCourseFileEntry(@NotNull File courseFile, @NotNull Module module)
      throws IOException {
    synchronized (courseFileLock) {
      JSONTokener tokenizer = new JSONTokener(new FileInputStream(courseFile));
      JSONObject jsonObject = new JSONObject(tokenizer);

      // It's possible that the "modules" key doesn't exist yet
      JSONObject modulesObject = jsonObject.optJSONObject("modules");
      if (modulesObject == null) {
        modulesObject = new JSONObject();
      }

      JSONObject entry = new JSONObject().put("id", module.getVersionId());
      entry.put("downloadedAt", LocalDateTime.now());
      modulesObject.put(module.getName(), entry);
      jsonObject.put("modules", modulesObject);
      FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);
    }
  }

  /**
   * Adds an entry for the given module to the course file. If an entry exists with the same name,
   * it is overwritten.
   * @param module The module for which an entry is added.
   * @throws IOException   If an IO error occurs (for an example, the course file doesn't exist).
   * @throws JSONException If the existing JSON in the course file is malformed.
   */
  public void addCourseFileEntry(@NotNull Module module) throws IOException {
    addCourseFileEntry(getCourseFilePath().toFile(), module);
  }

  /**
   * Parses the course file and returns a mapping of module names to their version IDs in the course
   * file. It's important to remember that a module entry in the course file does not necessarily
   * mean that the module is still in the project.
   * @return A map of module names to their local version IDs.
   * @throws IOException If an IO error occurs (for an example the course file doesn't exist).
   */
  @NotNull
  public Map<String, String> getCourseFileModuleIds() throws IOException {
    File courseFile = getCourseFilePath().toFile();
    Map<String, String> modules = new HashMap<>();
    synchronized (courseFileLock) {
      JSONTokener tokenizer = new JSONTokener(new FileInputStream(courseFile));
      JSONObject jsonObject = new JSONObject(tokenizer);

      // It's possible that the "modules" key doesn't exist
      JSONObject modulesObject = jsonObject.optJSONObject("modules");
      if (modulesObject == null) {
        return modules;
      }

      Iterable<String> moduleNames = modulesObject::keys;
      for (String moduleName : moduleNames) {
        String moduleId = modulesObject.getJSONObject(moduleName).getString("id");
        modules.put(moduleName, moduleId);
      }
      return modules;
    }
  }

  @NotNull
  public ModuleManager getModuleManager() {
    return ModuleManager.getInstance(project);
  }

  /**
   * Returns the root manager of the given module.
   * @param moduleName The name of the module.
   * @return The root manager of the module or null, if the module doesn't exist in the project.
   */
  @Nullable
  public ModuleRootManager getModuleRootManager(@NotNull String moduleName) {
    return Optional.ofNullable(getModuleManager().findModuleByName(moduleName))
        .map(ModuleRootManager::getInstance).orElse(null);
  }

  @NotNull
  public MessageBus getMessageBus() {
    return project.getMessageBus();
  }

  @NotNull
  public LibraryTable getLibraryTable() {
    return LibraryTablesRegistrar.getInstance().getLibraryTable(project);
  }

  /**
   * Returns a state in which the given component should be.
   * @param component An IntelliJ specific component.
   * @param <T> Type of the component.
   * @return Component state.
   */
  public <T extends Component & IntelliJComponent<?>> int resolveComponentState(
      @NotNull T component) {
    /*
     * Four cases to check for here:
     *   0. The component is in the project but the files are missing -> ERROR.
     *   1. The component is in the project, so its state should be INSTALLED.
     *   2. The component is not in the project but the module files are present in the file
     *      system, so its state should be FETCHED.
     *   3. The component files aren't present in the file system (and by extension the component
     *      isn't in the project), so its state should be NOT_INSTALLED.
     */
    boolean loaded = component.getPlatformObject() != null;
    boolean filesOk = doesDirExist(component.getPath());
    if (loaded && !filesOk) {
      return Component.ERROR;
    }
    if (loaded) {
      return Component.LOADED;
    }
    if (filesOk) {
      return Component.FETCHED;
    }
    return Component.NOT_INSTALLED;
  }

  public boolean doesDirExist(Path relativePath) {
    return getBasePath().resolve(relativePath).toFile().isDirectory();
  }
}
