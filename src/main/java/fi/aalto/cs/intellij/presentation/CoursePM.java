package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.Course;
import java.util.stream.Collectors;

public class CoursePM {
  private final Course course;
  private final ModuleListPM moduleListPM;

  public CoursePM(Course course) {
    this.course = course;
    moduleListPM = new ModuleListPM(course.getModules());
  }
}
