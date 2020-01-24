package fi.aalto.cs.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.Test;

public class FailedToLoadPluginsNotificationTest {

  @Test
  public void testFailToLoadPluginsNotification() {

    Notification notification = new FailedToLoadPluginsNotification();

    assertEquals("A+", notification.getGroupId());
    assertEquals("A+", notification.getTitle());
    assertEquals("A+ Course failed to download a list of available plugins from JetBrains'"
            + " plugin repository, please ensure Internet connectivity and restart the IDE.",
        notification.getContent());
    assertEquals(notification.getType(), NotificationType.ERROR);
  }

}