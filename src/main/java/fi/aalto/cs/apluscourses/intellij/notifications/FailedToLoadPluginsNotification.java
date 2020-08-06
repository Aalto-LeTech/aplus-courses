package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

/**
 * A {@link Notification} wrapper to let the user know about potential network issues when trying to
 * load plugins.
 */
public class FailedToLoadPluginsNotification extends Notification {

  /**
   * Builds the notification.
   */
  public FailedToLoadPluginsNotification() {
    super(
        getText("general.aPlus"),
        getText("notification.FailedToLoadPluginsNotification.title"),
        getText("notification.FailedToLoadPluginsNotification.content"),
        NotificationType.ERROR);
  }
}
