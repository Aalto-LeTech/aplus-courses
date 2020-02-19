package fi.aalto.cs.intellij.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnexpectedResponseHeadersException extends UnexpectedResponseException {
  public UnexpectedResponseHeadersException(@NotNull String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}
