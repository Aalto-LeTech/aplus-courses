package fi.aalto.cs.intellij.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnexpectedResponseBodyException extends RuntimeException {
  public UnexpectedResponseBodyException(@NotNull String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}
