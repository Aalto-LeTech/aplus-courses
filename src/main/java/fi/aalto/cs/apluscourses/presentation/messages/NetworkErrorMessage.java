package fi.aalto.cs.apluscourses.presentation.messages;

import org.jetbrains.annotations.NotNull;

public class NetworkErrorMessage implements Message {

  @NotNull
  private final Exception exception;

  public NetworkErrorMessage(@NotNull Exception exception) {
    this.exception = exception;
  }

  @NotNull
  @Override
  public String getContent() {
    return "Please check your network connection and try again. Error message: '"
        + exception.getMessage() + "'";
  }

  @NotNull
  @Override
  public String getTitle() {
    return "A+ Courses plugin encountered a network error";
  }

  @NotNull
  @Override
  public Level getLevel() {
    return Level.ERR;
  }

  @NotNull
  public Exception getException() {
    return exception;
  }
}
