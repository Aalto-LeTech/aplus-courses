package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.messages.MessageBus;
import fi.aalto.cs.apluscourses.model.Component;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
