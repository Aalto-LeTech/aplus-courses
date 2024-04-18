package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.services.PluginSettings;

public class SubmissionSentNotification extends Notification {

  /**
   * Construct a notification which informs the user that an exercise was submitted successfully.
   */
  public SubmissionSentNotification() {
    super(
        PluginSettings.A_PLUS,
        getText("notification.SuccessfulSubmissionNotification.title"),
        getText("notification.SuccessfulSubmissionNotification.content"),
        NotificationType.INFORMATION
    );
  }

}
