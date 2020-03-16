package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class IntelliJModelFactory implements ModelFactory {

  @NotNull
  private final Project project;

  public IntelliJModelFactory(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public Course createCourse(@NotNull String name,
                             @NotNull List<Module> modules,
                             @NotNull List<Library> libraries,
                             @NotNull Map<String, String> requiredPlugins) {
    return new IntelliJCourse(name, modules, libraries, requiredPlugins, project);
  }

  @Override
  public Module createModule(@NotNull String name, @NotNull URL url) {
    return new IntelliJModule(name, url, project);
  }

  @Override
  public Library createLibrary(@NotNull String name, @NotNull String type) {
    return null;
  }
}
