package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;

public class TaskCompleteNotification extends Notification {

  /**
   * Constructor.
   */
  public TaskCompleteNotification(String contentKey, int index) {
    super(PluginSettings.A_PLUS,
        getText("notification.TaskCompleteNotification.title"),
        getAndReplaceText(contentKey, index),
        NotificationType.INFORMATION);

  }

  public static TaskCompleteNotification createTaskCompleteNotification(int index) {
    return new TaskCompleteNotification("notification.TaskCompleteNotification.content", index + 1);
  }

  public static TaskCompleteNotification createTaskAlreadyCompleteNotification(int index) {
    return new TaskCompleteNotification("notification.TaskCompleteNotification.contentAlready", index + 1);
  }


}
