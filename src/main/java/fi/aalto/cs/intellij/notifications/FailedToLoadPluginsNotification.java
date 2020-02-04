package fi.aalto.cs.intellij.notifications;

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
    super("A+",
        "A+ Courses plugin internal error",
        "A+ Course failed to download a list of available plugins from JetBrains' plugin "
            + "repository, please ensure Internet connectivity and restart the IDE. If problem "
            + "persist, please contact the course administration.",
        NotificationType.ERROR);
  }
}
