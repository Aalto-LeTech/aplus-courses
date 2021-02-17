package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class ExerciseNotSelectedNotification extends Notification {

  /**
   * Constructs a notification that notifies the user that no exercise is selected. This should be
   * shown when the user uses the exercise submission button, but no exercise is selected.
   */
  public ExerciseNotSelectedNotification() {
    super(
        PluginSettings.A_PLUS,
        getText("notification.ExerciseNotSelectedNotification.title"),
        getText("notification.ExerciseNotSelectedNotification.content"),
        NotificationType.INFORMATION);
  }

}
