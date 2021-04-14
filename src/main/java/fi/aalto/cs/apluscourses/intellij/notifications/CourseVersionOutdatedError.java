package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;

public class CourseVersionOutdatedError extends Notification {
  /**
   * Notification to be shown when the plugin is outdated with respect to the current course.
   * This represents a mismatch in the major version and is an error.
   */
  public CourseVersionOutdatedError() {
    super(PluginSettings.A_PLUS, getText("notification.CourseVersionError.title"),
        getText("notification.CourseVersionOutdatedError.content"), NotificationType.ERROR);
  }
}
