package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoSuchComponentException extends Exception {
  public NoSuchComponentException(@NotNull String moduleName, @Nullable Throwable cause) {
    super("Module '" + moduleName + " was not found'.", cause);
  }
}
