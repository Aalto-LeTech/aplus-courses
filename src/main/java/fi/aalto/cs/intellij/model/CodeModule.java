package fi.aalto.cs.intellij.model;

import static java.util.stream.Stream.concat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CodeModule {

  private static final int FAILED = -1;
  private static final int UNINSTALLED = 0;
  private static final int INSTALLING = 1;
  private static final int INSTALLED = 2;

  private final Course course;
  private final String name;
  private final String url;
  private final List<String> dependencyList;
  private final Object dependencyLock = new Object();
  private volatile int state = UNINSTALLED;

  public CodeModule(@NotNull Course course, @NotNull String name, @NotNull String url) {
    this.course = course;
    this.name = name;
    this.url = url;
    dependencyList = new ArrayList<>();
  }

  public void registerDependency(String dependency) {
    synchronized (dependencyLock) {
      dependencyList.add(dependency);
    }
  }

  private List<CodeModule> getDependencies() {
    List<String> dependencies;
    synchronized (dependencyLock) {
      dependencies = new ArrayList<>(dependencyList);
    }
    return dependencies.stream().map(course::getModule).collect(Collectors.toList());
  }

  public Course getCourse() {
    return course;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void install() throws InstallationFailedException {
    try {
      installInternal();
    } catch (InstallationFailedRuntimeException e) {
      throw new InstallationFailedException(e.getCause());
    }
  }

  private void installInternal() throws InstallationFailedRuntimeException {
    if (state == INSTALLING || state == INSTALLED) {
      return;
    }
    state = INSTALLING;
    try {
      installAsync().join();
    } catch (CompletionException e) {
      state = FAILED;
      Throwable cause = e.getCause();
      if (cause instanceof InstallationFailedRuntimeException) {
        throw (InstallationFailedRuntimeException) cause;
      }
      throw e;
    }
    state = INSTALLED;
  }


  public CompletableFuture<Void> installAsync() {
    return CompletableFuture.allOf(
        concat(
            Stream.of(CompletableFuture.runAsync(this::installOverride)),
            getDependencies()
                .stream()
                .map(module -> CompletableFuture.runAsync(module::installInternal))
        ).toArray(CompletableFuture[]::new));
  }

  public String getPath() {
    return getName();
  }

  protected abstract void installOverride();

  public String getUrl() {
    return url;
  }

  protected class InstallationFailedException extends Exception {
    public InstallationFailedException(@Nullable Throwable throwable) {
      super("Cannot install code module '" + CodeModule.this.getName() + "'.", throwable);
    }

    public CodeModule getCodeModule() {
      return CodeModule.this;
    }
  }

  protected static class InstallationFailedRuntimeException extends RuntimeException {
    public InstallationFailedRuntimeException(@Nullable Throwable throwable) {
      super(throwable);
    }
  }
}
