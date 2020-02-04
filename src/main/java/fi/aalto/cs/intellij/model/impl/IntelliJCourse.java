package fi.aalto.cs.intellij.model.impl;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.intellij.common.Module;
import fi.aalto.cs.intellij.model.Course;
import java.util.List;
import java.util.Map;

public class IntelliJCourse extends Course {
  private final Project project;

  public IntelliJCourse(Project project) {
    this.project = project;
  }

  public Project getProject() {
    return project;
  }
}
