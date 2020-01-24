package fi.aalto.cs.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.List;
import org.junit.Test;

public class InstallPluginsNotificationTest extends PluginsTestHelper {

  @Test
  public void testEnablePluginsNotification() {

    List<IdeaPluginDescriptor> ideaPluginDescriptorListWithTwoValidValues =
        getDummyPluginsListOfTwo();
    Notification notification = new InstallPluginsNotification(
        ideaPluginDescriptorListWithTwoValidValues);

    assertEquals("the notification group is right", "A+", notification.getGroupId());
    assertEquals("A+", notification.getTitle());
    assertEquals(
        "The additional plugin(s) must be installed and enabled for the A+ plugin to work "
            + "properly (A+ Courses, Scala).",
        notification.getContent());
    assertEquals(notification.getType(), NotificationType.WARNING);
  }
}