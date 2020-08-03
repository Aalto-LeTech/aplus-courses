package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

public class ExerciseNotSelectedNotification extends Notification {

  /**
   * Constructs a notification that notifies the user that no exercise is selected. This should be
   * shown when the user uses the exercise submission button, but no exercise is selected.
   */
  public ExerciseNotSelectedNotification() {
    super(
        "A+",
        "No exercise is selected",
        "Select an exercise in the list and click the submission button to submit an exercise.",
        NotificationType.INFORMATION);
  }

}
