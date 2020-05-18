package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.messages.MessageBus;
import fi.aalto.cs.apluscourses.model.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class APlusProject {
  @NotNull
  private final Project project;

  public APlusProject(@NotNull Project project) {
    this.project = project;
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
  public synchronized boolean createCourseFile(@NotNull URL sourceUrl) throws IOException {
    File courseFile = getCourseFilePath().toFile();
    if (courseFile.isFile()) {
      return false;
    }

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("url", sourceUrl.toString());
    FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);
    return true;
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
