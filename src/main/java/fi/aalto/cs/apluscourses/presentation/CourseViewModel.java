package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.module.ModuleListViewModel;
import org.jetbrains.annotations.NotNull;

public class CourseViewModel extends BaseViewModel<Course> {

  @NotNull
  private final ModuleListViewModel modules;

  public CourseViewModel(Course course) {
    super(course);
    modules = new ModuleListViewModel(course.getModules());
  }

  @NotNull
  public ModuleListViewModel getModules() {
    return modules;
  }
}
