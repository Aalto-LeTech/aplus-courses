package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;

public class DownloadingDependenciesNotification extends Notification {
  public DownloadingDependenciesNotification(boolean done) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.DownloadingDependencies.title"),
        getText(done ? "notification.DownloadingDependencies.contentDone"
            : "notification.DownloadingDependencies.content"),
        NotificationType.INFORMATION);
  }
}
