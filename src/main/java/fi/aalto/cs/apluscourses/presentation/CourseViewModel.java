package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.presentation.module.ModuleListViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseViewModel extends BaseViewModel<Course> {

  @NotNull
  private final ModuleListViewModel modules;

  public CourseViewModel(@NotNull Course course, @NotNull Options moduleOptions) {
    super(course);
    modules = new ModuleListViewModel(course.getModules(), moduleOptions);
  }

  @NotNull
  public ModuleListViewModel getModules() {
    return modules;
  }
}
