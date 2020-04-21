package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.Module;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class IntelliJCourse extends Course {

  @NotNull
  private final APlusProject project;

  public IntelliJCourse(@NotNull String name,
                        @NotNull List<Module> modules,
                        @NotNull List<Library> libraries,
                        @NotNull Map<String, String> requiredPlugins,
                        @NotNull Map<String, URL> resourceUrls,
                        @NotNull APlusProject project) {
    super(name, modules, libraries, requiredPlugins, resourceUrls,
        new CommonLibraryProvider(project));

    this.project = project;
  }

  @NotNull
  public APlusProject getProject() {
    return project;
  }

  @Nullable
  public Component getComponentIfExists(VirtualFile file) {
    Component component = getComponentIfExists(file.getName());
    return component != null && component.getPath().toString().equals(file.getPath())
        ? component : null;
  }
}
