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

public class APlusProject {
  private final Project project;

  public APlusProject(Project project) {
    this.project = project;
  }

  @NotNull
  public Path getBasePath() {
    return Paths.get(Objects.requireNonNull(project.getBasePath()));
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
    return new LibraryStateResolver(name).resolveState();
  }

  public int resolveModuleState(String name) {
    return new ModuleStateResolver(name).resolveState();
  }

  private abstract class ComponentStateResolver {
    protected final String name;

    public ComponentStateResolver(String name) {
      this.name = name;
    }

    public int resolveState() {
      /*
       * Three cases to check for here:
       *   1. The component is in the project, so its state should be INSTALLED.
       *   2. The component is not in the project but the module files are present in the file
       *      system, so its state should be FETCHED.
       *   3. The component files aren't present in the file system (and by extension the component
       *      isn't in the project), so its state should be NOT_INSTALLED.
       */
      return isLoaded()
          ? Component.INSTALLED
          : getBasePath().resolve(getPath()).toFile().isDirectory()
          ? Component.FETCHED
          : Component.NOT_INSTALLED;
    }

    protected abstract boolean isLoaded();

    @NotNull
    protected abstract Path getPath();
  }

  private class ModuleStateResolver extends ComponentStateResolver {

    public ModuleStateResolver(String name) {
      super(name);
    }

    @Override
    protected boolean isLoaded() {
      return getModuleManager().findModuleByName(name) != null;
    }

    @NotNull
    @Override
    protected Path getPath() {
      return getModulePath(name);
    }
  }

  private class LibraryStateResolver extends ComponentStateResolver {

    public LibraryStateResolver(String name) {
      super(name);
    }

    @Override
    protected boolean isLoaded() {
      return getLibraryTable().getLibraryByName(name) != null;
    }

    @NotNull
    @Override
    protected Path getPath() {
      return getLibraryPath(name);
    }
  }
}
