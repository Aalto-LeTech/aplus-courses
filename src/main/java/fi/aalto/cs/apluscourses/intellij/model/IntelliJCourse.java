package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.Module;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

class IntelliJCourse extends Course {
  @NotNull
  private final Project project;

  IntelliJCourse(@NotNull String name,
                 @NotNull List<Module> modules,
                 @NotNull List<Library> libraries,
                 @NotNull Map<String, String> requiredPlugins,
                 @NotNull Project project) {
    super(name, modules, libraries, requiredPlugins);

    this.project = project;
  }

  @NotNull
  public Project getProject() {
    return project;
  }
}
