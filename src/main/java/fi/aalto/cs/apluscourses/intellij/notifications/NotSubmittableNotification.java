package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.services.PluginSettings;

public class NotSubmittableNotification extends Notification {

  /**
   * Construct a notification that explains that the exercise cannot be submitted from the plugin.
   */
  public NotSubmittableNotification() {
    super(
        PluginSettings.A_PLUS,
        getText("notification.NotSubmittableNotification.title"),
        getText("notification.NotSubmittableNotification.content"),
        NotificationType.ERROR
    );
  }

}
