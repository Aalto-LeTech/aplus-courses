package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.async.TaskManager;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ModuleInstallerImpl<T> implements ModuleInstaller {

  private final ModuleSource moduleSource;
  private final TaskManager<T> taskManager;

  public ModuleInstallerImpl(ModuleSource moduleSource, TaskManager<T> taskManager) {
    this.moduleSource = moduleSource;
    this.taskManager = taskManager;
  }

  protected T waitUntilLoadedAsync(Module module) {
    return taskManager.fork(() -> module.stateMonitor.waitUntil(Module.LOADED));
  }

  protected T installInternalAsync(Module module) {
    ModuleInstallation moduleInstallation = new ModuleInstallation(module);
    return taskManager.fork(moduleInstallation::doIt);
  }

  /**
   * Installs multiple modules and their dependencies.
   *
   * @param modules A {@link List} of {@link Module}s to be installed.
   */
  public void install(@NotNull List<Module> modules) {
    taskManager.joinAll(modules
        .stream()
        .map(this::installInternalAsync)
        .collect(Collectors.toList()));
  }

  /** Installs a module and its dependencies.
   *
   * @param module A {@link Module} to be installed.
   */
  public void install(Module module) {
    taskManager.join(installInternalAsync(module));
  }

  @Override
  public void installAsync(@NotNull List<Module> modules) {
    taskManager.fork(() -> install(modules));
  }

  private class ModuleInstallation {

    private final Module module;
    List<Module> dependencies;

    public ModuleInstallation(Module module) {
      this.module = module;
    }
    
    public void doIt() {
      try {
        fetch();
        load();
        waitForDependencies();
      } catch (IOException | ModuleLoadException e) {
        module.stateMonitor.set(Module.ERROR);
      }
    }
    
    private void fetch() throws IOException {
      if (module.stateMonitor.setConditionally(Module.NOT_INSTALLED, Module.FETCHING)) {
        module.fetch();
        module.stateMonitor.set(Module.FETCHED);
      } else {
        module.stateMonitor.waitUntil(Module.FETCHED);
      }
    }

    private void load() throws ModuleLoadException {
      if (module.stateMonitor.setConditionally(Module.FETCHED, Module.LOADING)) {
        installAsync(getDependencies());
        module.load();
        module.stateMonitor.set(Module.LOADED);
      } else {
        module.stateMonitor.waitUntil(Module.LOADED);
      }
    }

    private void waitForDependencies() throws ModuleLoadException {
      if (module.stateMonitor.setConditionally(Module.LOADED, Module.WAITING_FOR_DEPS)) {
        taskManager.joinAll(getDependencies()
            .stream()
            .map(ModuleInstallerImpl.this::waitUntilLoadedAsync)
            .collect(Collectors.toList()));
        module.stateMonitor.set(Module.INSTALLED);
      } else {
        module.stateMonitor.waitUntil(Module.INSTALLED);
      }
    }

    private List<Module> getDependencies() throws ModuleLoadException {
      try {
        if (dependencies == null) {
          dependencies = module.getDependencies()
              .stream()
              .map(moduleSource::getModule)
              .map(Objects::requireNonNull)
              .collect(Collectors.toList());
        }
        return dependencies;
      } catch (NullPointerException e) {
        throw new ModuleLoadException(module, e);
      }
    }
  }

  public static class FactoryImpl<T> implements ModuleInstaller.Factory {

    private final TaskManager<T> taskManager;

    public FactoryImpl(TaskManager<T> taskManager) {
      this.taskManager = taskManager;
    }

    @Override
    public ModuleInstaller getInstallerFor(Course course) {
      return new ModuleInstallerImpl<>(course, taskManager);
    }
  }
}
