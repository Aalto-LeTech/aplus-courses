package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

public class UnexpectedResponseException extends IOException {

  private static final long serialVersionUID = -3010286248078758468L;

  @NotNull
  private final transient HttpResponse response;

  /**
   * Constructs a {@link UnexpectedResponseException} with the given response and message.
   *
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
