package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.ReadAction;
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
import org.jetbrains.annotations.CalledWithReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusProject {

  @NotNull
  private final Project project;

  public APlusProject(@NotNull Project project) {
    this.project = project;
  }

  /**
   * Returns the IntelliJ IDEA project corresponding to this project. Throws {@link
   * IllegalStateException} if the project is disposed.
   * @return
   */
  @CalledWithReadLock
  @NotNull
  public Project getProject() {
    if (project.isDisposed()) {
      throw new IllegalStateException("Project is disposed.");
    }
    return project;
  }

  @NotNull
  public Path getBasePath() {
    return Paths.get(Objects.requireNonNull(ReadAction.compute(this::getProject).getBasePath()));
  }

  @CalledWithReadLock
  @NotNull
  public ModuleManager getModuleManager() {
    return ModuleManager.getInstance(getProject());
  }

  /**
   * Returns the root manager of the given module.
   *
   * @param moduleName The name of the module.
   * @return The root manager of the module or null, if the module doesn't exist in the project.
   */
  @CalledWithReadLock
  @Nullable
  public ModuleRootManager getModuleRootManager(@NotNull String moduleName) {
    return Optional.ofNullable(getModuleManager().findModuleByName(moduleName))
        .map(ModuleRootManager::getInstance).orElse(null);
  }

  @CalledWithReadLock
  @NotNull
  public MessageBus getMessageBus() {
    return getProject().getMessageBus();
  }

  @CalledWithReadLock
  @NotNull
  public LibraryTable getLibraryTable() {
    return LibraryTablesRegistrar.getInstance().getLibraryTable(getProject());
  }

  /**
   * Returns a state in which the given component should be.
   *
   * @param component An IntelliJ specific component.
   * @param <C>       Type of the component.
   * @return Component state.
   */
  public <C extends Component & IntelliJComponent<?>> int resolveComponentState(
      @NotNull C component) {
    return ReadAction.compute(new ComponentStateResolver<>(component)::resolve);
  }

  public boolean doesDirExist(Path relativePath) {
    return getBasePath().resolve(relativePath).toFile().isDirectory();
  }

  private class ComponentStateResolver<C extends Component & IntelliJComponent<?>> {
    private final C component;

    public ComponentStateResolver(C component) {
      this.component = component;
    }

    @CalledWithReadLock
    public int resolve() {
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
  }
}
