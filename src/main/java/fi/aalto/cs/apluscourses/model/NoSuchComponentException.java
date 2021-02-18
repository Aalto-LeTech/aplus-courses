package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoSuchComponentException extends Exception {

  private static final long serialVersionUID = 8978356915281214148L;

  public NoSuchComponentException(@NotNull String componentName, @Nullable Throwable cause) {
    super("Component '" + componentName + " was not found'.", cause);
  }
}
