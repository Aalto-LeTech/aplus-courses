package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentSource;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.NoSuchComponentException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CommonLibraryProvider implements ComponentSource {
  private final APlusProject project;
  private final ConcurrentMap<String, Library> commonLibraries;

  public CommonLibraryProvider(APlusProject project) {
    this.project = project;
    commonLibraries = new ConcurrentHashMap<>();
  }

  @NotNull
  @Override
  public Component getComponent(@NotNull String name) throws NoSuchComponentException {
    Library library = commonLibraries.computeIfAbsent(name, this::createLibrary);
    if (library == null) {
      throw new NoSuchComponentException(name, null);
    }
    return library;
  }

  @Nullable
  @Override
  public Component getComponentIfExists(@NotNull String name) {
    return commonLibraries.getOrDefault(name, null);
  }

  private Library createLibrary(String name) {
    if (name.startsWith("scala-sdk-")) {
      return new ScalaSdk(name, project, project.resolveLibraryState(name));
    }
    return null;
  }
}
