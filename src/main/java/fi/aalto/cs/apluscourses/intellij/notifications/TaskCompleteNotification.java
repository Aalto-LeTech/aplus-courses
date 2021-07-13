package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class TaskCompleteNotification extends Notification {



  public TaskCompleteNotification(@NotNull String moduleName) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.MissingModuleNotification.title"),
        getAndReplaceText("notification.MissingModuleNotification.content", moduleName),
        NotificationType.ERROR);

  }
}
