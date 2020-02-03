package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.Course;
import jdk.internal.jline.internal.Nullable;

public class MainModel {

  private volatile ModuleListModel modules;

  public void setCourse(Course course) {
    modules = new ModuleListModel(course.getModules());
  }

  @Nullable
  public ModuleListModel getModules() {
    return modules;
  }
}
