package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.async.TaskManager;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentInstallerImpl<T> implements ComponentInstaller {

  private static final Logger logger = LoggerFactory.getLogger(ComponentInstallerImpl.class);

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

    public Installation(Component component) {
      this.component = component;
    }
    
    public void doIt() {
      component.resolveState();
      try {
        fetch();
        load();
        waitForDependencies();
      } catch (IOException | ComponentLoadException | NoSuchComponentException e) {
        logger.info("A component could not be installed", e);
        component.stateMonitor.set(Component.ERROR);
      }
    }
    
    private void fetch() throws IOException {
      if (component.stateMonitor.setConditionally(Component.NOT_INSTALLED, Component.FETCHING)) {
        component.fetch();
        component.stateMonitor.set(Component.FETCHED);
      } else {
        component.stateMonitor.waitUntil(Component.FETCHED);
      }
    }

    private void load() throws ComponentLoadException {
      if (component.stateMonitor.setConditionally(Component.FETCHED, Component.LOADING)) {
        component.load();
        component.stateMonitor.set(Component.LOADED);
      } else {
        component.stateMonitor.waitUntil(Component.LOADED);
      }
    }

    private void waitForDependencies() throws NoSuchComponentException {
      if (component.dependencyStateMonitor.setConditionally(Component.DEP_INITIAL,
          Component.DEP_WAITING)) {
        List<Component> dependencies = componentSource.getComponents(component.getDependencies());
        installAsync(dependencies);
        taskManager.joinAll(dependencies
            .stream()
            .map(ComponentInstallerImpl.this::waitUntilLoadedAsync)
            .collect(Collectors.toList()));
        component.dependencyStateMonitor.set(Component.DEP_VALIDATING);
        component.dependencyStateMonitor.set(component.checkDependencyIntegrity(componentSource)
            ? Component.DEP_LOADED : Component.DEP_ERROR);
      } else {
        component.dependencyStateMonitor.waitUntil(Component.DEP_LOADED);
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
