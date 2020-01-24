package fi.aalto.cs.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.List;
import org.junit.Test;

public class EnablePluginsTest extends PluginsTestHelper {

  @Test
  public void testEnablePluginsNotification() {

    List<IdeaPluginDescriptor> ideaPluginDescriptorListWithTwoValidValues =
        getDummyPluginsListOfTwo();
    Notification notification = new EnablePluginsNotification(
        ideaPluginDescriptorListWithTwoValidValues);

    assertEquals("A+", notification.getGroupId());
    assertEquals("A+", notification.getTitle());
    assertEquals(
        "Some plugins must be and enabled for the A+ plugin to work properly "
            + "(A+ Courses, Scala).", notification.getContent());
    assertEquals(notification.getType(), NotificationType.WARNING);
  }
}