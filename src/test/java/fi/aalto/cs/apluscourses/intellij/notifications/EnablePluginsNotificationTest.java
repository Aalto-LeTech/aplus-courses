package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.TestHelper.getDummyPluginsListOfTwo;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.TestHelper;
import java.util.List;
import org.junit.Test;

public class EnablePluginsNotificationTest extends TestHelper {

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
            + "(A+ Courses, Liferay IntelliJ Plugin).", notification.getContent());
    assertEquals("The type of the notification should be 'WARNING'.", notification.getType(),
        NotificationType.WARNING);
  }
}
