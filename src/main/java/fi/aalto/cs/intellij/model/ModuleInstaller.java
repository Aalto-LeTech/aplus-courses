package fi.aalto.cs.intellij.model;

import fi.aalto.cs.intellij.utils.TaskManager;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ModuleInstaller<T> {

  private final ModuleSource moduleSource;
  private final TaskManager<T> taskManager;

  public ModuleInstaller(ModuleSource moduleSource, TaskManager<T> taskManager) {
    this.moduleSource = moduleSource;
    this.taskManager = taskManager;
  }

  private void install(Module module) {
    new ModuleInstallation(module).doIt();
  }

  /**
   * Installs multiple modules using possibly asynchronous execution.  See
   * {@code installAsync(Module)} for further info.
   * @param modules A {@link List} of {@link Module}s.
   * @return A future given by {@code TaskManager.fork()}.
   */
  public T installAsync(@NotNull List<Module> modules) {
    return taskManager.all(modules
        .stream()
        .map(this::installAsync)
        .collect(Collectors.toList()));
  }

  /** Installs a module and its dependencies using possibly asynchronous execution provided by the
   * {@link TaskManager} of this installer and returns a future object with which
   * {@code TaskManager.join()} can be called.
   *
   * <p>Note that it is guaranteed that when {@code TaskManager.join()} is called with the object
   * returned by this method, the state of the module is at least LOADED (or error) but not
   * necessarily INSTALLED.  However, it is guaranteed that the state will eventually be changed to
   * INSTALLED (or error).</p>
   *
   * @param module A module to be installed.
   * @return A future given by {@code TaskManager.fork()}.
   */
  public T installAsync(Module module) {
    return taskManager.fork(() -> install(module));
  }

  class ModuleInstallation {

    private final Module module;

    public ModuleInstallation(Module module) {
      this.module = module;
    }
    
    private void doIt() {
      T installDependenciesTask;
      try {
        fetch();
        installDependenciesTask = installAsync(getDependencies());
        load();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      } catch (IOException | ModuleLoadException e) {
        module.stateMonitor.set(Module.ERROR);
        return;
      }
      end(installDependenciesTask);
    }
    
    void fetch() throws IOException, InterruptedException {
      if (module.stateMonitor.setConditionally(Module.NOT_INSTALLED, Module.FETCHING)) {
        module.fetch();
        module.stateMonitor.set(Module.FETCHED);
      } else {
        module.stateMonitor.waitUntil(Module.FETCHED);
      }
    }

    void load() throws ModuleLoadException, InterruptedException {
      if (module.stateMonitor.setConditionally(Module.FETCHED, Module.LOADING)) {
        module.load();
        module.stateMonitor.set(Module.LOADED);
      } else {
        module.stateMonitor.waitUntil(Module.LOADED);
      }
    }

    void end(T installDependenciesTask) {
      if (module.stateMonitor.setConditionally(Module.LOADED, Module.WAITING_FOR_DEPS)) {
        taskManager.join(installDependenciesTask);
        module.stateMonitor.set(Module.INSTALLED);
      }
    }

    List<Module> getDependencies() throws IOException, ModuleLoadException {
      return module.getDependencies()
          .stream()
          .map(moduleSource::getModule)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
  }
}
