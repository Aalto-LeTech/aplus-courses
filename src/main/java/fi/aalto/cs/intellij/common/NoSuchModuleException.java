package fi.aalto.cs.intellij.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoSuchModuleException extends Exception {
  @NotNull
  private final Course course;

  public NoSuchModuleException(@NotNull Course course, @NotNull String message,
                               @Nullable Throwable cause) {
    super(message, cause);
    this.course = course;
  }

  @NotNull
  public Course getCourse() {
    return course;
  }
}
