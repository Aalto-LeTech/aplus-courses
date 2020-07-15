package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

public class NotSubmittableNotification extends Notification {

  /**
   * Construct a notification that explains that the exercise cannot be submitted from the plugin.
   */
  public NotSubmittableNotification() {
    super(
        "A+",
        "Cannot submit exercise",
        "This exercise can only be submitted from the A+ web interface.",
        NotificationType.ERROR
    );
  }

}
