package fi.aalto.cs.intellij.notifications;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.List;
import org.junit.Test;

public class EnablePluginsNotificationTest extends PluginsTestHelper {

  @Test
  public void testEnablePluginsNotification() {

    List<IdeaPluginDescriptor> ideaPluginDescriptorListWithTwoValidValues =
        getDummyPluginsListOfTwo();
    Notification notification = new EnablePluginsNotification(
        ideaPluginDescriptorListWithTwoValidValues);

    assertEquals("Group ID should be 'A+'.", "A+", notification.getGroupId());
    assertEquals("Title should be 'A+ Courses plugin required plugins disabled warning'.",
        "A+ Courses plugin required plugins disabled warning", notification.getTitle());
    assertEquals("Content should contain the names of the plugins to enable.",
        "Some plugins must be and enabled for the A+ plugin to work properly "
            + "(A+ Courses, Scala).", notification.getContent());
    assertEquals("The type of the notification should be 'WARNING'.", notification.getType(),
        NotificationType.WARNING);
  }
}