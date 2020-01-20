package fi.aalto.cs.intellij.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mockito;

public class EnablePluginsActionTest {

  @Test
  public void test_actionPerformed() throws IOException, JDOMException {
    AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
    Notification notification = Mockito.mock(Notification.class);

    IdeaPluginDescriptorImpl ideaPluginDescriptor = getIdeaPluginDescriptor();
    ideaPluginDescriptor.setEnabled(false);
    List<IdeaPluginDescriptor> descriptors = new ArrayList<>();
    descriptors.add(ideaPluginDescriptor);

    assertFalse(ideaPluginDescriptor.isEnabled());

    EnablePluginsAction enableMissingPluginsAction = new EnablePluginsAction("",
        descriptors);
    enableMissingPluginsAction.actionPerformed(anActionEvent, notification);

    assertTrue(ideaPluginDescriptor.isEnabled());
  }

  @NotNull
  private IdeaPluginDescriptorImpl getIdeaPluginDescriptor() throws IOException, JDOMException {
    File filePath = new File("src/test/resources/plugins/a+/plugin.xml");
    IdeaPluginDescriptorImpl ideaPluginDescriptor = new IdeaPluginDescriptorImpl(filePath, false);
    ideaPluginDescriptor.loadFromFile(filePath, null, true);
    return ideaPluginDescriptor;
  }
}