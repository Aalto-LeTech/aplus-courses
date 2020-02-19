package fi.aalto.cs.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public class ClientIoError extends Notification {

  @NotNull
  private final Exception exception;

  public ClientIoError(@NotNull Exception exception) {
    // Tell the user to restart for now as a temporary solution.
    super("A+", "A+ Courses plugin encountered a network error",
        "Please check your network connection and restart IntelliJ IDEA.",
        NotificationType.ERROR);
    this.exception = exception;
  }

  @NotNull
  public Exception getException() {
    return exception;
  }
}
