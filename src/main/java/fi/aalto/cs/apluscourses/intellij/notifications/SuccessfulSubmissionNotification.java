package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

public class SuccessfulSubmissionNotification extends Notification {

  /**
   * Construct a notification which informs the user that an exercise was submitted successfully.
   */
  public SuccessfulSubmissionNotification() {
    super(
        "A+",
        "Exercise submitted successfully",
        "You will be notified when feedback for the exercise is available.",
        NotificationType.INFORMATION
    );
  }

}
