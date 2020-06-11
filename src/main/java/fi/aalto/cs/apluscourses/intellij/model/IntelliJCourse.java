package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.Module;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class IntelliJCourse extends Course {

  @NotNull
  private final APlusProject project;

  private final CommonLibraryProvider commonLibraryProvider;

  public IntelliJCourse(@NotNull String id,
                        @NotNull String name,
                        @NotNull List<Module> modules,
                        @NotNull List<Library> libraries,
                        @NotNull Map<String, String> requiredPlugins,
                        @NotNull Map<String, URL> resourceUrls,
                        @NotNull List<String> autoInstallComponentNames,
                        @NotNull APlusProject project,
                        @NotNull CommonLibraryProvider commonLibraryProvider) {
    super(id, name, modules, libraries, requiredPlugins, resourceUrls, autoInstallComponentNames);

    this.project = project;
    this.commonLibraryProvider = commonLibraryProvider;
  }

  @NotNull
  public APlusProject getProject() {
    return project;
  }

  @NotNull
  public CommonLibraryProvider getCommonLibraryProvider() {
    return commonLibraryProvider;
  }

  @Nullable
  @Override
  public Component getComponentIfExists(@NotNull String name) {
    Component component = super.getComponentIfExists(name);
    return component != null ? component : commonLibraryProvider.getComponentIfExists(name);
  }

  @Nullable
  public Component getComponentIfExists(VirtualFile file) {
    Component component = getComponentIfExists(file.getName());
    Path pathOfTheFile = Paths.get(file.getPath());
    return component != null && component.getFullPath().equals(pathOfTheFile) ? component : null;
  }

  @NotNull
  @Override
  public Collection<Component> getComponents() {
    return Stream.concat(
        super.getComponents().stream(),
        commonLibraryProvider.getProvidedLibraries().stream()
    ).collect(Collectors.toCollection(ArrayList::new));
  }
}
