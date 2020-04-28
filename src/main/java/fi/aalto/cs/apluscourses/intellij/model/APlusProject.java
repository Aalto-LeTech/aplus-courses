package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.messages.MessageBus;
import fi.aalto.cs.apluscourses.model.Component;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    return getBasePath().resolve(Paths.get(Project.DIRECTORY_STORE_FOLDER, "a-plus-project"));
  }

  @NotNull
  public ModuleManager getModuleManager() {
    return ModuleManager.getInstance(project);
  }

  @NotNull
  public MessageBus getMessageBus() {
    return project.getMessageBus();
  }

  @NotNull
  public LibraryTable getLibraryTable() {
    return LibraryTablesRegistrar.getInstance().getLibraryTable(project);
  }

  @NotNull
  public Path getLibraryPath(String name) {
    return  Paths.get("lib", name);
  }

  @NotNull
  public Path getModulePath(String name) {
    return Paths.get(name);
  }

  public int resolveLibraryState(String name) {
    return resolveComponentState(getLibraryTable().getLibraryByName(name), getLibraryPath(name));
  }

  public int resolveModuleState(String name) {
    return resolveComponentState(getModuleManager().findModuleByName(name), getModulePath(name));
  }

  private int resolveComponentState(@Nullable Object componentObj, @NotNull Path dirPath) {
    /*
     * Three cases to check for here:
     *   1. The component is in the project, so its state should be INSTALLED.
     *   2. The component is not in the project but the module files are present in the file
     *      system, so its state should be FETCHED.
     *   3. The component files aren't present in the file system (and by extension the component
     *      isn't in the project), so its state should be NOT_INSTALLED.
     */
    if (componentObj != null) {
      return Component.INSTALLED;
    }
    if (doesDirExist(dirPath)) {
      return Component.FETCHED;
    }
    return Component.NOT_INSTALLED;
  }

  private boolean doesDirExist(Path relativePath) {
    return getBasePath().resolve(relativePath).toFile().isDirectory();
  }
}
