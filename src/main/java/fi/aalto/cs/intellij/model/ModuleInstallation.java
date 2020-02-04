package fi.aalto.cs.intellij.model;

import com.intellij.openapi.application.WriteAction;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class ModuleInstallation {

  private final Module module;
  private final ModuleSource moduleSource;

  public ModuleInstallation(@NotNull Module module, @NotNull ModuleSource moduleSource) {
    this.module = module;
    this.moduleSource = moduleSource;
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
  private ModuleInstallation newInstallation(@NotNull Module module) {
    return new ModuleInstallation(module, moduleSource);
  }

  public CompletableFuture<Void> installAsync() {
    return CompletableFuture.runAsync(this::install);
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
      CompletableFuture<Void> installDependenciesFuture = installDependenciesAsync();
      new Loader().load();
      module.stateMonitor.set(Module.LOADED);
      installDependenciesFuture.join();
      module.stateMonitor.set(Module.INSTALLED);
    } else {
      module.stateMonitor.waitFor(Module.LOADED);
    }
  }

  private CompletableFuture<Void> installDependenciesAsync() throws IOException {
    return CompletableFuture.allOf(module.getDependencies()
        .stream()
        .map(moduleSource::getModule)
        .filter(Objects::nonNull)
        .map(this::newInstallation)
        .map(ModuleInstallation::installAsync)
        .toArray(CompletableFuture[]::new)
    );
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
