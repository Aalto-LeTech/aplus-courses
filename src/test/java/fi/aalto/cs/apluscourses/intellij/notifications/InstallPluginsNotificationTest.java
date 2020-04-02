package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.TestHelper.getDummyPluginsListOfTwo;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import java.util.List;
import org.junit.Test;

public class InstallPluginsNotificationTest extends BasePlatformTestCase implements TestHelper {

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
            + "properly (A+ Courses, Liferay IntelliJ Plugin).",
        notification.getContent());
    assertEquals("The type of the notification should be 'WARNING'.",
        notification.getType(), NotificationType.WARNING);
  }
}
