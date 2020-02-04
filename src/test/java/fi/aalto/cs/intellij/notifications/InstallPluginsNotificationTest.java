package fi.aalto.cs.intellij.notifications;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.List;
import org.junit.Test;

public class InstallPluginsNotificationTest extends PluginsTestHelper {

  @Test
  public void testInstallPluginsNotification() {

    List<IdeaPluginDescriptor> ideaPluginDescriptorListWithTwoValidValues =
        getDummyPluginsListOfTwo();
    Notification notification = new InstallPluginsNotification(
        ideaPluginDescriptorListWithTwoValidValues);

    assertEquals("Group ID should be 'A+'.", "A+", notification.getGroupId());
    assertEquals("Title should be 'A+ Courses plugin required plugins missing warning'.",
        "A+ Courses plugin required plugins missing warning", notification.getTitle());
    assertEquals("Content should contain the names of the plugins to install.",
        "The additional plugin(s) must be installed and enabled for the A+ plugin to work "
            + "properly (A+ Courses, Scala).",
        notification.getContent());
    assertEquals("The type of the notification should be 'WARNING'.",
        notification.getType(), NotificationType.WARNING);
  }
}