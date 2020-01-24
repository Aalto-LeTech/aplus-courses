package fi.aalto.cs.intellij.actions;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.intellij.notifications.EnablePluginsNotification;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.List;
import org.junit.Test;

public class EnablePluginsNotificationActionTest extends PluginsTestHelper {

  @Test
  public void testActionPerformed() {

    List<IdeaPluginDescriptor> ideaPluginDescriptorListWithTwoValidValues = getDummyPluginsListOfTwo();

    EnablePluginsNotificationAction enablePluginsNotificationAction =
        new EnablePluginsNotificationAction(ideaPluginDescriptorListWithTwoValidValues);
    Notification notification = mock(EnablePluginsNotification.class);
    ideaPluginDescriptorListWithTwoValidValues.forEach(descriptor -> descriptor.setEnabled(false));

    assertFalse(ideaPluginDescriptorListWithTwoValidValues.get(0).isEnabled());
    assertFalse(ideaPluginDescriptorListWithTwoValidValues.get(1).isEnabled());

    enablePluginsNotificationAction.actionPerformed(mock(AnActionEvent.class), notification);

    

  }
}