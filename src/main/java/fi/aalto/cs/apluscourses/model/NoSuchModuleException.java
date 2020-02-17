package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoSuchModuleException extends Exception {
  public NoSuchModuleException(@NotNull String moduleName, @Nullable Throwable cause) {
    super("Module '" + moduleName + " was not found'.", cause);
  }
}
