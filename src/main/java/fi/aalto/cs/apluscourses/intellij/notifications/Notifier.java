package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Notifier {
  void notify(@NotNull Notification notification, @Nullable Project project);

  void notifyAndHide(@NotNull Notification notification, @Nullable Project project);
}
