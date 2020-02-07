package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public class CourseConfigurationError extends Notification {

  private final Exception exception;

  /**
   * Constructs a notification that notifies the user of an error that occurred while attempting to
   * parse a course configuration file.
   * @param exception An exception that caused this notification.
   */
  public CourseConfigurationError(@NotNull Exception exception) {
    super("A+", "A+ Courses plugin failed to parse the course configuration file",
        "Many features of the plugin won't work as expected. Please contact the course staff "
            + "about the issue. Error message: " + exception.getMessage(),
        NotificationType.ERROR);
    this.exception = exception;
  }
  
  public Exception getException() {
    return exception;
  }
}
