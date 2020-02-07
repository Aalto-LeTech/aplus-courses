package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import fi.aalto.cs.apluscourses.PluginsTestHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class EnablePluginsNotificationActionTest extends PluginsTestHelper {

  @Test
  public void testActionPerformed() {
    // given
    List<IdeaPluginDescriptor> descriptorList = getDummyPluginsListOfTwo();
    Map<PluginId, IdeaPluginDescriptor> descriptors = new HashMap<>();

    descriptorList.forEach(descriptor -> {
      descriptor.setEnabled(false);
      descriptors.put(descriptor.getPluginId(), descriptor);
    });

    assertFalse("The first created dummy plugin is disabled.",
        descriptorList.get(0).isEnabled());
    assertFalse("The second created dummy plugin is disabled.",
        descriptorList.get(1).isEnabled());

    //when
    EnablePluginsNotificationAction enableMissingPluginsAction =
        new EnablePluginsNotificationAction(new ArrayList<>(descriptors.values()),
            descriptors::get);

    AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
    Notification notification = Mockito.mock(Notification.class);
    enableMissingPluginsAction.actionPerformed(anActionEvent, notification);

    //then
    assertTrue("The first created dummy plugin is enabled.",
        descriptorList.get(0).isEnabled());
    assertTrue("The second created dummy plugin is enabled.",
        descriptorList.get(1).isEnabled());
    assertTrue("Action text contains a name of a first dummy plugin.",
        enableMissingPluginsAction.getTemplateText().contains("Scala"));
    assertTrue("Action text contains a name of a second dummy plugin.",
        enableMissingPluginsAction.getTemplateText().contains("A+ Courses"));
    assertTrue("Action text contains the base part of the notification.",
        enableMissingPluginsAction.getTemplateText().contains("Enable the required plugin(s)"));
    verify(notification, times(1)).expire();
  }
}
