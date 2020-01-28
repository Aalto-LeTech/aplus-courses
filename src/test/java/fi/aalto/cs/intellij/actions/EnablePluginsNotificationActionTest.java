package fi.aalto.cs.intellij.actions;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class EnablePluginsNotificationActionTest extends PluginsTestHelper {

  @Test
  public void testActionPerformed() {
    List<IdeaPluginDescriptor> descriptorList = getDummyPluginsListOfTwo();
    Map<PluginId, IdeaPluginDescriptor> descriptors = new HashMap<>();

    descriptorList.forEach(descriptor -> {
      descriptor.setEnabled(false);
      descriptors.put(descriptor.getPluginId(), descriptor);
    });

    assertFalse(descriptorList.get(0).isEnabled());
    assertFalse(descriptorList.get(1).isEnabled());

    EnablePluginsNotificationAction enableMissingPluginsAction =
        new EnablePluginsNotificationAction(new ArrayList<>(descriptors.values()),
            descriptors::get);

    AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
    Notification notification = Mockito.mock(Notification.class);
    enableMissingPluginsAction.actionPerformed(anActionEvent, notification);

    assertTrue(descriptorList.get(0).isEnabled());
    assertTrue(descriptorList.get(1).isEnabled());
    assertTrue(enableMissingPluginsAction.getTemplateText().contains("Scala"));
    assertTrue(enableMissingPluginsAction.getTemplateText().contains("A+ Courses"));
    assertTrue(
        enableMissingPluginsAction.getTemplateText().contains("Enable the required plugin(s)"));
    verify(notification, times(1)).expire();
  }
}