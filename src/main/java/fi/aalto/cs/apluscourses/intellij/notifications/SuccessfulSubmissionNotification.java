package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

public class SuccessfulSubmissionNotification extends Notification {

  /**
   * Construct a notification which informs the user that an exercise was submitted successfully.
   */
  public SuccessfulSubmissionNotification() {
    super(
        "A+",
        getText("notification.SuccessfulSubmissionNotification.title"),
        getText("notification.SuccessfulSubmissionNotification.content"),
        NotificationType.INFORMATION
    );
  }

}
