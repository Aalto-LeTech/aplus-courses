package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.async.TaskManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentInstallerImpl<T> implements ComponentInstaller {

  private static Logger logger = LoggerFactory.getLogger(ComponentInstallerImpl.class);

  private final ComponentSource componentSource;
  private final TaskManager<T> taskManager;

  public ComponentInstallerImpl(ComponentSource componentSource, TaskManager<T> taskManager) {
    this.componentSource = componentSource;
    this.taskManager = taskManager;
  }

  protected T waitUntilLoadedAsync(Component component) {
    return taskManager.fork(() -> component.stateMonitor.waitUntil(Component.LOADED));
  }

  protected T installInternalAsync(Component component) {
    Installation installation = new Installation(component);
    return taskManager.fork(installation::doIt);
  }

  /**
   * Installs multiple components and their dependencies.
   *
   * @param components A {@link List} of {@link Component}s to be installed.
   */
  @Override
  public void install(@NotNull List<Component> components) {
    taskManager.joinAll(components
        .stream()
        .map(this::installInternalAsync)
        .collect(Collectors.toList()));
  }

  /** Installs a component and its dependencies.
   *
   * @param component A {@link Component} to be installed.
   */
  @Override
  public void install(Component component) {
    taskManager.join(installInternalAsync(component));
  }

  @Override
  public void installAsync(@NotNull List<Component> components) {
    taskManager.fork(() -> install(components));
  }

  private class Installation {

    private final Component component;
    List<Component> dependencies;

    public Installation(Component component) {
      this.component = component;
    }
    
    public void doIt() {
      try {
        fetch();
        initDependencies();
        load();
        waitForDependencies();
      } catch (IOException | ComponentLoadException | NoSuchComponentException e) {
        logger.info("A component could not be installed", e);
        component.stateMonitor.set(Component.ERROR);
      }
    }
    
    private void fetch() throws IOException {
      if (component.stateMonitor.setConditionally(Component.NOT_INSTALLED, Component.FETCHING)
          || component.stateMonitor.setConditionally(Component.UNINSTALLED, Component.FETCHING)) {
        component.fetch();
        component.stateMonitor.set(Component.FETCHED);
      } else {
        component.stateMonitor.waitUntil(Component.FETCHED);
      }
    }

    private void load() throws ComponentLoadException {
      if (component.stateMonitor.setConditionally(Component.FETCHED, Component.LOADING)
          || component.stateMonitor.setConditionally(Component.UNLOADED, Component.LOADING)) {
        installAsync(dependencies);
        component.load();
        component.stateMonitor.set(Component.LOADED);
      } else {
        component.stateMonitor.waitUntil(Component.LOADED);
      }
    }

    private void waitForDependencies() {
      if (component.stateMonitor.setConditionally(Component.LOADED, Component.WAITING_FOR_DEPS)) {
        taskManager.joinAll(dependencies
            .stream()
            .map(ComponentInstallerImpl.this::waitUntilLoadedAsync)
            .collect(Collectors.toList()));
        component.stateMonitor.set(Component.INSTALLED);
      } else {
        component.stateMonitor.waitUntil(Component.INSTALLED);
      }
    }

    private void initDependencies() throws ComponentLoadException, NoSuchComponentException {
      if (dependencies != null) {
        return;
      }
      List<String> dependencyNames = component.getDependencies();
      dependencies = new ArrayList<>(dependencyNames.size());
      for (String dependencyName : dependencyNames) {
        dependencies.add(componentSource.getComponent(dependencyName));
      }
    }
  }

  public static class FactoryImpl<T> implements ComponentInstaller.Factory {

    private final TaskManager<T> taskManager;

    public FactoryImpl(TaskManager<T> taskManager) {
      this.taskManager = taskManager;
    }

    @Override
    public ComponentInstaller getInstallerFor(ComponentSource componentSource) {
      return new ComponentInstallerImpl<>(componentSource, taskManager);
    }
  }
}
