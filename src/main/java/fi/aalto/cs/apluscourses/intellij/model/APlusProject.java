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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
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

  private static final Object courseFileLock = new Object();

  private static final String COURSE_FILE_MODULES_KEY = "modules";

  /**
   * Adds an entry for the given module to the given course file. If an entry exists with the same
   * name, then the existing entry is overwritten.
   *
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
      JSONObject modulesObject = jsonObject.optJSONObject(COURSE_FILE_MODULES_KEY);
      if (modulesObject == null) {
        modulesObject = new JSONObject();
      }

      JSONObject entry = new JSONObject().put("id", module.getVersionId());
      entry.put("downloadedAt", ZonedDateTime.now());
      modulesObject.put(module.getName(), entry);
      jsonObject.put(COURSE_FILE_MODULES_KEY, modulesObject);
      FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);
    }
  }

  /**
   * Parses the course file and returns a mapping of module names to its {@link
   * IntelliJModuleMetadata}. It's important to remember that a module entry in the course file does
   * not necessarily mean that the module is still in the project.
   *
   * @return A map of module names to their local {@link IntelliJModuleMetadata}.
   * @throws IOException If an IO error occurs (for an example the course file doesn't exist).
   */
  @NotNull
  public Map<String, IntelliJModuleMetadata> getCourseFileModuleMetadata() throws IOException {
    synchronized (courseFileLock) {
      File courseFile = getCourseFilePath().toFile();
      Map<String, IntelliJModuleMetadata> modules = new HashMap<>();

      JSONTokener tokenizer = new JSONTokener(new FileInputStream(courseFile));
      JSONObject jsonObject = new JSONObject(tokenizer);

      // It's possible that the "modules" key doesn't exist
      JSONObject modulesObject = jsonObject.optJSONObject(COURSE_FILE_MODULES_KEY);
      if (modulesObject == null) {
        return modules;
      }

      Iterable<String> moduleNames = modulesObject::keys;
      for (String moduleName : moduleNames) {
        JSONObject moduleObject = modulesObject.getJSONObject(moduleName);
        String moduleId = moduleObject.getString("id");
        ZonedDateTime downloadedAt = ZonedDateTime.parse(moduleObject.getString("downloadedAt"));
        IntelliJModuleMetadata intelliJModuleMetadata = new IntelliJModuleMetadata(moduleId,
            downloadedAt);
        modules.put(moduleName, intelliJModuleMetadata);
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
   *
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
   *
   * @param component An IntelliJ specific component.
   * @param <T>       Type of the component.
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
