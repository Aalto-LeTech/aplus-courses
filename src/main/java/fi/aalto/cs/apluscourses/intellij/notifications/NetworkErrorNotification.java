package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public class NetworkErrorNotification extends Notification {

  /**
   * Constructs a notification that notifies the user of an IO error arising from the HTTP client.
   * @param exception An exception that caused this notification.
   */
  public NetworkErrorNotification(@NotNull Exception exception) {
    // Tell the user to restart for now as a temporary solution.
    super("A+", "A+ Courses plugin encountered a network error",
        "Please check your network connection and try again. Error message: '"
            + exception.getMessage() + "'.",
        NotificationType.ERROR);
  }

}
