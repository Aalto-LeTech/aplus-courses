package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;

public class CourseVersionOutdatedWarning extends Notification {
  /**
   * Notification to be shown when the plugin is outdated with respect to the current course.
   * This represents a mismatch in the minor version and is a warning.
   */
  public CourseVersionOutdatedWarning() {
    super(PluginSettings.A_PLUS, getText("notification.CourseVersionError.title"),
        getText("notification.CourseVersionOutdatedWarning.content"), NotificationType.WARNING);
  }
}
