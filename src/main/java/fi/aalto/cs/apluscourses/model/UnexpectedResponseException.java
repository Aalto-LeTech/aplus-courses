package fi.aalto.cs.apluscourses.model;

import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class UnexpectedResponseException extends IOException {
  @NotNull
  private final HttpResponse response;

  /**
   * Constructs a {@link UnexpectedResponseException} with the given response and message.
   * @param response The response to which this exception relates.
   * @param message  The message.
   */
  public UnexpectedResponseException(@NotNull HttpResponse response,
                                     @NotNull String message) {
    super(message);
    this.response = response;
  }

  @NotNull
  public HttpResponse getResponse() {
    return response;
  }
}
