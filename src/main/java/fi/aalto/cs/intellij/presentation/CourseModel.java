package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.model.Course;
import fi.aalto.cs.intellij.presentation.base.BaseModel;
import fi.aalto.cs.intellij.presentation.module.ModuleListModel;
import org.jetbrains.annotations.NotNull;

public class CourseModel extends BaseModel<Course> {

  @NotNull
  private final ModuleListModel modules;

  public CourseModel(Course course) {
    super(course);
    modules = new ModuleListModel(course.getModules());
  }

  @NotNull
  public ModuleListModel getModules() {
    return modules;
  }
}
