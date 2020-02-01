package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.Course;

public class CourseModel {
  private final Course course;
  private final ModuleListModel moduleListPM;

  public CourseModel(Course course) {
    this.course = course;
    moduleListPM = new ModuleListModel(course.getModules());
  }

  public ModuleListModel getModuleListPM() {
    return moduleListPM;
  }
}
