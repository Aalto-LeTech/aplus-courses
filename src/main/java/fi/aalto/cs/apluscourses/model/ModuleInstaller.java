package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.TaskManager;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleInstaller<T> {

  private final ModuleSource moduleSource;
  private final TaskManager<T> taskManager;

  public ModuleInstaller(ModuleSource moduleSource, TaskManager<T> taskManager) {
    this.moduleSource = moduleSource;
    this.taskManager = taskManager;
  }

  private T installInternal(List<Module> modules) {
    return taskManager.forkAll(modules
        .stream()
        .map(module -> (Runnable) () -> installInternal(module))
        .collect(Collectors.toList()));
  }

  /*
   * When this method returns, the state is guaranteed to be at least LOADED (or error).
   * When {@code TaskManager.join()} has returned from call with the object returned by this method,
   * the state of the module is guaranteed to be INSTALLED (or error).
   */
  private T installInternal(Module module) {
    ModuleInstallation moduleInstallation = new ModuleInstallation(module);
    return moduleInstallation.doIt();
  }

  /**
   * Installs multiple modules and their dependencies.
   *
   * @param modules A {@link List} of {@link Module}s to be installed.
   *
   */
  public void install(@NotNull List<Module> modules) {
    taskManager.forkAllAndJoin(modules
        .stream()
        .map(module -> (Runnable) () -> install(module))
        .collect(Collectors.toList()));
  }

  /** Installs a module and its dependencies.
   *
   * @param module A {@link Module} to be installed.
   */
  public void install(Module module) {
    taskManager.join(installInternal(module));
  }

  class ModuleInstallation {

    private final Module module;

    public ModuleInstallation(Module module) {
      this.module = module;
    }
    
    private T doIt() {
      T installDependenciesTask;
      List<Module> dependencies;
      try {
        fetch();
        dependencies = getDependencies();
        installDependenciesTask = load(dependencies);
      } catch (IOException | ModuleLoadException e) {
        module.stateMonitor.set(Module.ERROR);
        return null;
      }
      if (installDependenciesTask == null) {
        return null;
      }
      return taskManager.fork(() -> end(installDependenciesTask, dependencies));
    }
    
    void fetch() throws IOException {
      if (module.stateMonitor.setConditionally(Module.NOT_INSTALLED, Module.FETCHING)) {
        module.fetch();
        module.stateMonitor.set(Module.FETCHED);
      } else {
        module.stateMonitor.waitUntil(Module.FETCHED);
      }
    }

    T load(List<Module> dependencies) throws ModuleLoadException {
      if (module.stateMonitor.setConditionally(Module.FETCHED, Module.LOADING)) {
        T installDependenciesTask = installInternal(dependencies);
        module.load();
        module.stateMonitor.set(Module.LOADED);
        return installDependenciesTask;
      } else {
        module.stateMonitor.waitUntil(Module.LOADED);
        return null;
      }
    }

    void end(T installDependenciesTask, List<Module> dependencies) {
      module.stateMonitor.set(Module.WAITING_FOR_DEPS);
      taskManager.join(installDependenciesTask);
      module.stateMonitor.set(dependencies.stream().anyMatch(Module::hasError)
          ? Module.ERROR
          : Module.INSTALLED);
    }

    List<Module> getDependencies() throws IOException, ModuleLoadException {
      try {
        return module.getDependencies()
            .stream()
            .map(moduleSource::getModule)
            .map(Objects::requireNonNull)
            .collect(Collectors.toList());
      } catch (NullPointerException e) {
        throw new ModuleLoadException(module, e);
      }
    }
  }
}
