package fi.aalto.cs.intellij.model;

import com.intellij.openapi.application.WriteAction;
import fi.aalto.cs.intellij.utils.TaskManager;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ModuleInstallation<T> {

  private final Module module;
  private final ModuleSource moduleSource;
  private final TaskManager<T> taskManager;

  public ModuleInstallation(@NotNull Module module,
                            @NotNull ModuleSource moduleSource,
                            TaskManager<T> taskManager) {
    this.module = module;
    this.moduleSource = moduleSource;
    this.taskManager = taskManager;
  }

  public void install() {
    try {
      fetch();
      load();
    } catch (IOException e) {
      module.stateMonitor.set(Module.ERROR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @NotNull
  private ModuleInstallation<T> newInstallation(@NotNull Module module) {
    return new ModuleInstallation<>(module, moduleSource, taskManager);
  }

  public T installAsync() {
    return taskManager.fork(this::install);
  }

  private void fetch() throws IOException, InterruptedException {
    if (module.stateMonitor.setConditionally(Module.NOT_INSTALLED, Module.FETCHING)) {
      module.fetch();
      module.stateMonitor.set(Module.FETCHED);
    } else {
      module.stateMonitor.waitFor(Module.FETCHED);
    }
  }

  private void load() throws IOException, InterruptedException {
    if (module.stateMonitor.setConditionally(Module.FETCHED, Module.LOADING)) {
      T installDependenciesTask = installDependenciesAsync();
      new Loader().load();
      module.stateMonitor.set(Module.LOADED);
      taskManager.join(installDependenciesTask);
      module.stateMonitor.set(Module.INSTALLED);
    } else {
      module.stateMonitor.waitFor(Module.LOADED);
    }
  }

  private void installDependencies() {
    try {
      for (T task : module.getDependencies()
          .stream()
          .map(moduleSource::getModule)
          .filter(Objects::nonNull)
          .map(this::newInstallation)
          .map(ModuleInstallation::installAsync).collect(Collectors.toList()))
        taskManager.join(task);
    } catch (IOException e) {
      module.stateMonitor.set(Module.ERROR);
    }
  }

  private T installDependenciesAsync() throws IOException {
    return taskManager.fork(this::installDependencies);
  }

  private class Loader {
    // Sonar does not like non-primitive-type volatile fields because apparently people easily
    // misunderstand their semantics, but we know what we are doing here.
    private volatile IOException exception = null; //NOSONAR

    public void load() throws IOException {
      WriteAction.runAndWait(this::loadInternal);
      IOException e = exception;
      if (e != null) {
        throw e;
      }
    }

    private void loadInternal() {
      try {
        module.load();
      } catch (IOException e) {
        exception = e;
      }
    }
  }
}
