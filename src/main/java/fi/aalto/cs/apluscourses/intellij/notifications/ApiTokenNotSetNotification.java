package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

public class ApiTokenNotSetNotification extends Notification {
  /**
   * Constructs a notification that tells authentication is not set.
   */
  public ApiTokenNotSetNotification() {
    super("A+",
        "API token not stored",
        "Token could not be persistently stored. You will be requested to paste the token again "
        + "next time you'll open the project. To allow token to be securely stored in your "
        + "machine, check your keyring settings.",
        NotificationType.INFORMATION);
  }
}
