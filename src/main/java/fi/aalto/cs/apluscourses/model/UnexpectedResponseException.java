package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnexpectedResponseException extends IOException {
  @NotNull
  private final HttpResponse response;

  /**
   * Constructs a {@link UnexpectedResponseException} with the given response, message, and cause.
   * @param response The response to which this exception relates.
   * @param message  The message.
   * @param cause    The cause of this exception or null.
   */
  public UnexpectedResponseException(@NotNull HttpResponse response,
                                     @NotNull String message,
                                     @Nullable Throwable cause) {
    super(message, cause);
    this.response = response;
  }

  @NotNull
  HttpResponse getResponse() {
    return response;
  }
}
