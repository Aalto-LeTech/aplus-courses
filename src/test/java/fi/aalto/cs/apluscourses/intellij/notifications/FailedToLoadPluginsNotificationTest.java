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
    assertEquals("Title should be 'A+'.", "A+", notification.getTitle());
    assertEquals("Content should inform a user about potential network malfunction.",
        "A+ Course failed to download a list of available plugins from JetBrains'"
            + " plugin repository, please ensure Internet connectivity and restart the IDE.",
        notification.getContent());
    assertEquals("The type of the notification should be 'ERROR'.", notification.getType(),
        NotificationType.ERROR);
  }
}
