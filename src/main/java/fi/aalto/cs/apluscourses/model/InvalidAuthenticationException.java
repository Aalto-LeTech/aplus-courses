package fi.aalto.cs.apluscourses.model;

import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

public class InvalidAuthenticationException extends UnexpectedResponseException {
  public InvalidAuthenticationException(@NotNull HttpResponse response,
                                        @NotNull String message) {
    super(response, message);
  }
}
