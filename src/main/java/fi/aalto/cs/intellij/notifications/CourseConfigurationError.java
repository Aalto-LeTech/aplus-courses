package fi.aalto.cs.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public class CourseConfigurationError extends Notification {
  /**
   * Constructs a notification that notifies the user of an error that occurred while attempting to
   * parse a course configuration file.
   * @param errorMessage An error message containing details that are intended to be useful for
   *                     course staff and plugin developers.
   */
  public CourseConfigurationError(@NotNull String errorMessage) {
    super("A+", "A+ Courses plugin failed to the parse course configuration file",
        "Many features of the plugin won't work as expected. Please contact the course staff "
            + "about the issue. Error message: " + errorMessage,
        NotificationType.ERROR);
  }
}
