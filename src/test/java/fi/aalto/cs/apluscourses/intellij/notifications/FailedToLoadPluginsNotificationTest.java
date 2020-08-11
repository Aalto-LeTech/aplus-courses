package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.Test;

public class FailedToLoadPluginsNotificationTest {

  @Test
  public void testFailToLoadPluginsNotification() {

    Notification notification = new FailedToLoadPluginsNotification();

    assertEquals("Group ID should be 'A+'.", "A+", notification.getGroupId());
    assertEquals("Title should be 'A+ Courses plugin internal error'.",
        "Error in the A+ Courses plugin", notification.getTitle());
    assertEquals("Content should inform a user about potential network malfunction.",
        "A+ Courses failed to download a list of available IntelliJ plugins from "
            + "JetBrains'' repository. Please ensure your internet connection is available and "
            + "restart the IDE. If the problem persists, please contact the course staff.",
        notification.getContent());
    assertEquals("The type of the notification should be 'ERROR'.", notification.getType(),
        NotificationType.ERROR);
  }
}
