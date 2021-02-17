package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class CourseConfigurationError extends Notification {

  private final Exception exception;

  /**
   * Constructs a notification that notifies the user of an error that occurred while attempting to
   * parse a course configuration file.
   *
   * @param exception An exception that caused this notification.
   */
  public CourseConfigurationError(@NotNull Exception exception) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.CourseConfigurationError.title"),
        getAndReplaceText("notification.CourseConfigurationError.content",
            exception.getMessage()),
        NotificationType.ERROR);
    this.exception = exception;
  }

  public Exception getException() {
    return exception;
  }
}
