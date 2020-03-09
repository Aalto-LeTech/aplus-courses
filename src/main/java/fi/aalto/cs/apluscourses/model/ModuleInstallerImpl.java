package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.async.TaskManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleInstallerImpl<T> implements ModuleInstaller {

  private static Logger logger = LoggerFactory.getLogger(ModuleInstallerImpl.class);

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
  @Override
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
  @Override
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
        initDependencies();
        load();
        waitForDependencies();
      } catch (IOException | ModuleLoadException | NoSuchModuleException e) {
        logger.info("A module could not be installed", e);
        module.stateMonitor.set(Module.ERROR);
      }
    }
    
    private void fetch() throws IOException {
      if (module.stateMonitor.setConditionally(Module.NOT_INSTALLED, Module.FETCHING)
          || module.stateMonitor.setConditionally(Module.UNINSTALLED, Module.FETCHING)) {
        module.fetch();
        module.stateMonitor.set(Module.FETCHED);
      } else {
        module.stateMonitor.waitUntil(Module.FETCHED);
      }
    }

    private void load() throws ModuleLoadException {
      if (module.stateMonitor.setConditionally(Module.FETCHED, Module.LOADING)
          || module.stateMonitor.setConditionally(Module.UNLOADED, Module.LOADING)) {
        installAsync(dependencies);
        module.load();
        module.stateMonitor.set(Module.LOADED);
      } else {
        module.stateMonitor.waitUntil(Module.LOADED);
      }
    }

    private void waitForDependencies() {
      if (module.stateMonitor.setConditionally(Module.LOADED, Module.WAITING_FOR_DEPS)) {
        taskManager.joinAll(dependencies
            .stream()
            .map(ModuleInstallerImpl.this::waitUntilLoadedAsync)
            .collect(Collectors.toList()));
        module.stateMonitor.set(Module.INSTALLED);
      } else {
        module.stateMonitor.waitUntil(Module.INSTALLED);
      }
    }

    private void initDependencies() throws ModuleLoadException, NoSuchModuleException {
      if (dependencies != null) {
        return;
      }
      List<String> dependencyNames = module.getDependencies();
      dependencies = new ArrayList<>(dependencyNames.size());
      for (String dependencyName : dependencyNames) {
        dependencies.add(moduleSource.getModule(dependencyName));
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
