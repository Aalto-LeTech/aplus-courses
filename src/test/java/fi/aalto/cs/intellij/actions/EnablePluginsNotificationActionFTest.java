package fi.aalto.cs.intellij.actions;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.ide.plugins.newui.BgProgressIndicator;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jdom.JDOMException;
import org.junit.Test;
import org.mockito.Mockito;

public class EnablePluginsNotificationActionFTest extends PluginsTestHelper {

  @Test
  public void testActionPerformed() {
    List<IdeaPluginDescriptor> descriptorList = new ArrayList<>();
    IdeaPluginDescriptor ideaPluginDescriptor = getIdeaCorePluginDescriptor();
    descriptorList.add(ideaPluginDescriptor);
    ideaPluginDescriptor.setEnabled(false);
    assertFalse(ideaPluginDescriptor.isEnabled());

    EnablePluginsNotificationAction enableMissingPluginsAction =
        new EnablePluginsNotificationAction(descriptorList);

    AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
    Notification notification = Mockito.mock(Notification.class);
    enableMissingPluginsAction.actionPerformed(anActionEvent, notification);

    assertTrue(descriptorList.get(0).isEnabled());
    assertEquals("Enable the required plugin(s) (IDEA CORE).",
        enableMissingPluginsAction.getTemplateText());
    verify(notification, times(1)).expire();
    assertTrue(ideaPluginDescriptor.isEnabled());
  }
}
