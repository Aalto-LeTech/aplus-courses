package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoSuchModuleException extends Exception {
  @NotNull
  private final Course course;

  public NoSuchModuleException(@NotNull Course course, @NotNull String moduleName,
                               @Nullable Throwable cause) {
    super("Course '" + course.getName() + "' has no module '" + moduleName + "'.", cause);
    this.course = course;
  }

  @NotNull
  public Course getCourse() {
    return course;
  }
}
