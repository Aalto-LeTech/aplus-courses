package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoSuchComponentException extends Exception {
  public NoSuchComponentException(@NotNull String componentName, @Nullable Throwable cause) {
    super("Component '" + componentName + " was not found'.", cause);
  }
}
