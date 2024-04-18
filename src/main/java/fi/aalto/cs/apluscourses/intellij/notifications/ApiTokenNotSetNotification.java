package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.services.PluginSettings;

public class ApiTokenNotSetNotification extends Notification {
  /**
   * Constructs a notification that tells authentication is not set.
   */
  public ApiTokenNotSetNotification() {
    super(
        PluginSettings.A_PLUS,
        getText("notification.ApiTokenNotSet.title"),
        getText("notification.ApiTokenNotSet.content"),
        NotificationType.INFORMATION);
  }
}
