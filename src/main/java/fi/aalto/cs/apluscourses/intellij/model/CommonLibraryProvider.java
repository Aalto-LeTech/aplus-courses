package fi.aalto.cs.apluscourses.intellij.model;

import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentSource;
import fi.aalto.cs.apluscourses.model.Library;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommonLibraryProvider implements ComponentSource {
  private final APlusProject project;
  private final ConcurrentMap<String, Library> commonLibraries;
  private volatile Component.InitializationCallback initializationCallback; //  NOSONAR

  public CommonLibraryProvider(@NotNull APlusProject project) {
    this.project = project;
    commonLibraries = new ConcurrentHashMap<>();
  }

  @Nullable
  @Override
  public Component getComponentIfExists(@NotNull String name) {
    // computeIfAbsent: If the function returns null no mapping is recorded.
    return commonLibraries.computeIfAbsent(name,
        ((Function<String, Library>) this::createLibrary).andThen(this::initialize));
  }

  public void setInitializationCallback(
      @Nullable Component.InitializationCallback initializationCallback) {
    this.initializationCallback = initializationCallback;
  }

  @Nullable
  protected Library createLibrary(@NotNull String name) {
    if (name.startsWith("scala-sdk-3")) {
      return new Scala3Sdk(name, name.replace("scala-sdk-", ""), project);
    } else if (name.startsWith("scala-sdk-")) {
      return new ScalaSdk(name, name.replace("scala-sdk-", ""), project);
    } else if (name.startsWith("scala-library-")) {
      return new ScalaLibrary(name, name.replace("scala-library-", ""), project);
    }
    return null;
  }

  @Nullable
  private Library initialize(@Nullable Library library) {
    if (library != null && initializationCallback != null) {
      initializationCallback.initialize(library);
    }
    return library;
  }

  @NotNull
  public Collection<Library> getProvidedLibraries() {
    return commonLibraries.values();
  }

}
