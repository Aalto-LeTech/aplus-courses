package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentSource;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.NoSuchModuleException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;

class CommonLibraryProvider implements ComponentSource {
  private final Project project;
  private final ConcurrentMap<String, Library> commonLibraries;

  public CommonLibraryProvider(Project project) {
    this.project = project;
    commonLibraries = new ConcurrentHashMap<>();
  }

  @NotNull
  @Override
  public Component getComponent(@NotNull String componentName) throws NoSuchModuleException {
    Library library = commonLibraries.computeIfAbsent(componentName, this::createLibrary);
    if (library == null) {
      throw new NoSuchModuleException(componentName, null);
    }
    return library;
  }

  private Library createLibrary(String libraryName) {
    if (libraryName.startsWith("scala-sdk-")) {
      return new ScalaSdk(libraryName, project);
    }
    return null;
  }
}
