package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface Notifier {
  void notify(@NotNull Notification notification, @Nullable Project project);

  Logger logger = LoggerFactory.getLogger(Notifier.class);

  /**
   * Shows a notification and hides it after 6 seconds.
   * @param notification The Notification to be shown.
   * @param project The Project where the Notification is shown.
   */
  static void notifyAndHide(@NotNull Notification notification, @Nullable Project project) {
    Notifications.Bus.notify(notification, project);
    Executors.newSingleThreadExecutor().submit(() -> {
      try {
        Thread.sleep(6000);
        notification.expire();
      } catch (InterruptedException e) {
        logger.error("Notification timeout interrupted", e);
        Thread.currentThread().interrupt();
      }
      return null;
    });
  }
}
