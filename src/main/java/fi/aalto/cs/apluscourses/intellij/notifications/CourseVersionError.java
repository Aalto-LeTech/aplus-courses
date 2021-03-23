package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;

public class CourseVersionError extends Notification {
  /**
   * Notification to be shown when the plugin is outdated with respect to the current course.
   *
   * @param isError If true, the notification is an error. Otherwise, it is a warning.
   */
  public CourseVersionError(boolean isError) {
    super(PluginSettings.A_PLUS, getText("notification.CourseVersionError.title"),
          getText(isError ? "notification.CourseVersionError.contentError"
                          : "notification.CourseVersionError.contentWarning"),
          isError ? NotificationType.ERROR : NotificationType.WARNING);
  }
}
