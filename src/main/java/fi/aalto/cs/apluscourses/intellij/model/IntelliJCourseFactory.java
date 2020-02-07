package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.CourseFactory;
import fi.aalto.cs.apluscourses.model.Module;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class IntelliJCourseFactory implements CourseFactory {

  @NotNull
  private final Project project;

  public IntelliJCourseFactory(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public Course createCourse(@NotNull String name,
                             @NotNull List<Module> modules,
                             @NotNull Map<String, String> requiredPlugins) {
    return new IntelliJCourse(name, modules, requiredPlugins, project);
  }

  @Override
  public Module createModule(@NotNull String name, @NotNull URL url) {
    // Change this when modules are no longer fetched from a local dir.
    return new LocalFetchingIntelliJModule(name, url, project);
  }
}
