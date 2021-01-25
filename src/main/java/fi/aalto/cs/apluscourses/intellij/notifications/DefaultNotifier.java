package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultNotifier implements Notifier {
  public void notify(@NotNull Notification notification, @Nullable Project project) {
    Notifications.Bus.notify(notification, project);
  }

  public void notifyAndHide(@NotNull Notification notification, @Nullable Project project) {
    NotificationUtil.notifyAndHide(notification, project);
  }
}
