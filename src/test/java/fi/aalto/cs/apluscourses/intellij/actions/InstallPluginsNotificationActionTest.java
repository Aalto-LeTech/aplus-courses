package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.PluginsTestHelper;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class InstallPluginsNotificationActionTest extends PluginsTestHelper {

  @Test(expected = RuntimeException.class)
  public void testActionPerformed() {
    //given
    AtomicInteger numberOfCallsOfInstall = new AtomicInteger(0);
    AtomicInteger numberOfCallsOfRestartProposal = new AtomicInteger(0);
    List<IdeaPluginDescriptor> descriptorList = getDummyPluginsListOfTwo();

    AnActionEvent anActionEvent = mock(AnActionEvent.class);
    Notification notification = mock(Notification.class);

    //when
    InstallPluginsNotificationAction installPluginsNotificationAction =
        new InstallPluginsNotificationAction(
            descriptorList,
            descriptor -> numberOfCallsOfInstall.getAndIncrement(),
            numberOfCallsOfRestartProposal::getAndIncrement);
    installPluginsNotificationAction.actionPerformed(anActionEvent, notification);

    //then

    verify(notification, times(1)).expire();
    assertEquals("Installation method should be called required amount of times.", 2,
        numberOfCallsOfInstall.get());
    assertEquals("Restart proposing functionality should have been called once.", 1,
        numberOfCallsOfRestartProposal.get());
  }
}
