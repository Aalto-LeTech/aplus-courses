package fi.aalto.cs.apluscourses.model;

import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnexpectedResponseStatusException extends UnexpectedResponseException {
  @NotNull
  private final StatusLine statusLine;

  public UnexpectedResponseStatusException(@NotNull StatusLine statusLine,
                                           @Nullable Throwable cause) {
    super("Unexpected status line in HTTP response", cause);
    this.statusLine = statusLine;
  }

  public StatusLine getStatusLine() {
    return statusLine;
  }
}
