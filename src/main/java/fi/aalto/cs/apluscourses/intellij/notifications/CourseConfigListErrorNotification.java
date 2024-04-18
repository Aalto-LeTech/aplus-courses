package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.services.PluginSettings;

public class CourseConfigListErrorNotification extends Notification {

  /**
   * Constructs a notification that notifies the user of an error about course configs.
   */
  public CourseConfigListErrorNotification() {
    super(
        PluginSettings.A_PLUS,
        getText("notification.CourseConfigListErrorNotification.title"),
        getText("notification.CourseConfigListErrorNotification.content"),
        NotificationType.ERROR);
  }

}

