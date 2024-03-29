package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NotificationUtil {
  private static final Logger logger = APlusLogger.logger;

  private NotificationUtil() {
  }

  /**
   * Shows a notification and hides it after 6 seconds.
   *
   * @param notification The Notification to be shown.
   * @param project      The Project where the Notification is shown.
   */
  public static void notifyAndHide(@NotNull Notification notification, @Nullable Project project) {
    // IntelliJ implementation of notifyAndHide is missing the project parameter from notify
    Notifications.Bus.notify(notification, project);
    Executors.newSingleThreadExecutor().submit(() -> {
      try {
        Thread.sleep(6000);
        notification.hideBalloon();
      } catch (InterruptedException e) {
        logger.warn("Notification timeout interrupted", e);
        Thread.currentThread().interrupt();
      }
      return null;
    });
  }
}
