package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.presentation.MainViewModelUpdater;
import fi.aalto.cs.apluscourses.utils.async.Awaitable;
import fi.aalto.cs.apluscourses.utils.async.TaskManager;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentInstallerImpl<T> implements ComponentInstaller {

  private static final Logger logger = LoggerFactory.getLogger(ComponentInstallerImpl.class);

  private final ComponentSource componentSource;
  private final TaskManager<T> taskManager;
  private final Dialogs dialogs;

  /**
   * Constructor.
   *
   * @param componentSource E.g. a {@link Course} object.
   * @param taskManager     A manager object who orchestrates asynchronous execution.
   * @param dialogs         Dialogs to be shown during the installation.
   */
  public ComponentInstallerImpl(@NotNull ComponentSource componentSource,
                                @NotNull TaskManager<T> taskManager,
                                @NotNull Dialogs dialogs) {
    this.componentSource = componentSource;
    this.taskManager = taskManager;
    this.dialogs = dialogs;
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
  public Awaitable installAsync(@NotNull List<Component> components, @Nullable Runnable callback) {
    return taskManager.run(() -> {
      install(components);
      if (callback != null) {
        callback.run();
      }
    });
  }

  private class Installation {

    private final Component component;

    public Installation(Component component) {
      this.component = component;
    }
    
    public void doIt() {
      MainViewModelUpdater.prevent();
      component.resolveState();
      unloadIfError();
      try {
        if (component.isUpdatable()) {
          uninstallForUpdate();
        }
        fetch();
        load();
        waitForDependencies();
        component.validate(componentSource);
      } catch (IOException | ComponentLoadException | NoSuchComponentException e) {
        logger.info("A component could not be installed", e);
        component.stateMonitor.set(Component.ERROR);
      } finally {
        MainViewModelUpdater.enable();
      }
    }

    private void unloadIfError() {
      if (component.stateMonitor.hasError()) {
        component.unload();
        component.setUnresolved();
        component.resolveState();
      }
    }

    private void uninstallForUpdate() throws IOException {
      if (component.stateMonitor.setConditionallyTo(Component.UNINSTALLING, Component.LOADED)) {
        if (component.hasLocalChanges() && !dialogs.shouldOverwrite(component)) {
          abortAction();
          return;
        }
        component.unload();
        component.remove();
        component.stateMonitor.set(Component.UNINSTALLED);
      }
    }

    private void abortAction() {
      if (component.stateMonitor.setConditionallyTo(Component.ACTION_ABORTED,
          Component.FETCHING, Component.LOADING, Component.UNINSTALLING)) {
        component.setUnresolved();
        component.resolveState();
      }
    }

    private void fetch() throws IOException {
      if (component.stateMonitor.setConditionallyTo(Component.FETCHING,
          Component.NOT_INSTALLED, Component.UNINSTALLED)) {
        component.fetch();
        component.stateMonitor.set(Component.FETCHED);
      } else {
        component.stateMonitor.waitUntil(Component.FETCHED);
      }
    }

    private void load() throws ComponentLoadException {
      if (component.stateMonitor.setConditionallyTo(Component.LOADING, Component.FETCHED)) {
        component.load();
        component.stateMonitor.set(Component.LOADED);
      } else {
        component.stateMonitor.waitUntil(Component.LOADED);
      }
    }

    private void waitForDependencies() throws NoSuchComponentException {
      if (component.dependencyStateMonitor.setConditionallyTo(Component.DEP_WAITING,
          Component.DEP_INITIAL, Component.DEP_ERROR)) {
        List<Component> dependencies = componentSource.getComponents(component.getDependencies());
        dependencies.forEach(Component::resolveState);
        installAsync(dependencies);
        taskManager.joinAll(dependencies
            .stream()
            .map(ComponentInstallerImpl.this::waitUntilLoadedAsync)
            .collect(Collectors.toList()));
        component.dependencyStateMonitor.set(Component.DEP_LOADED);
      } else {
        component.dependencyStateMonitor.waitUntil(Component.DEP_LOADED);
      }
    }
  }

  public static class FactoryImpl<T> implements ComponentInstaller.Factory {
    @NotNull
    private final TaskManager<T> taskManager;

    public FactoryImpl(@NotNull TaskManager<T> taskManager) {
      this.taskManager = taskManager;
    }

    @Override
    @NotNull
    public ComponentInstaller getInstallerFor(@NotNull ComponentSource componentSource,
                                              @NotNull Dialogs dialogs) {
      return new ComponentInstallerImpl<>(componentSource, taskManager, dialogs);
    }
  }
}
