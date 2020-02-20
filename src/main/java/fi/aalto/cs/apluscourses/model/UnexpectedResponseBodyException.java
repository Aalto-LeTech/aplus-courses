package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnexpectedResponseBodyException extends RuntimeException {
  public UnexpectedResponseBodyException(@NotNull String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}
