package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.NoSuchModuleException;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

class IntelliJCourse extends Course {
  @NotNull
  private final Project project;

  IntelliJCourse(@NotNull String name,
                 @NotNull List<Module> modules,
                 @NotNull Map<String, String> requiredPlugins,
                 @NotNull Project project) {
    super(name, modules, requiredPlugins);

    this.project = project;
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  /**
   * This method updates the states of the module objects, when the given IntelliJ module is removed
   * from the IntelliJ project of this course.
   */
  public void onModuleRemove(@NotNull com.intellij.openapi.module.Module projectModule) {
    try {
      Module module = getModule(projectModule.getName());
      module.updateState();
      List<Module> dependents = getModulesDependentOn(module);
      for (Module dependentModule : dependents) {
        // TODO: actually set the states of dependent modules to LOADED
        System.out.println(dependentModule.getName());
      }
    } catch (NoSuchModuleException e) {
      throw new IllegalStateException("Course missing module from project", e);
    }
  }
}
