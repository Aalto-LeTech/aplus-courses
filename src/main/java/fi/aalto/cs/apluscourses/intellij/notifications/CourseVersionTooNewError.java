package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;

public class CourseVersionTooNewError extends Notification {
  /**
   * Notification to be shown when the plugin is too new to support the current course.
   */
  public CourseVersionTooNewError() {
    super(PluginSettings.A_PLUS, getText("notification.CourseVersionError.title"),
        getText("notification.CourseVersionTooNewError.content"), NotificationType.ERROR);
  }
}
