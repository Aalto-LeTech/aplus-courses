package fi.aalto.cs.intellij.actions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class EnablePluginsNotificationActionTest extends PluginsTestHelper {

  @Test
  public void testActionPerformed() {
    //given
    AtomicInteger numberOfCallsOfInstall = new AtomicInteger(0);
    AtomicInteger numberOfCallsOfRestartProposal = new AtomicInteger(0);
    List<IdeaPluginDescriptor> descriptorList = getDummyPluginsListOfTwo();

    AnActionEvent anActionEvent = mock(AnActionEvent.class);
    Notification notification = mock(Notification.class);

    //when
    EnablePluginsNotificationAction enablePluginsNotificationAction =
        new EnablePluginsNotificationAction(
            descriptorList,
            descriptor -> {
              numberOfCallsOfInstall.getAndIncrement();
              return true;
            },
            numberOfCallsOfRestartProposal::getAndIncrement);
    enablePluginsNotificationAction.actionPerformed(anActionEvent, notification);

    //then
    verify(notification, times(1)).expire();
    assertEquals("Enable method should be called required amount of times.", 2,
        numberOfCallsOfInstall.get());
    assertEquals("Restart proposing functionality should have been called once.", 1,
        numberOfCallsOfRestartProposal.get());
  }

  @Test
  public void testProposeRestart() {
    assertThrows(
        RuntimeException.class,
        "Restart IntelliJ IDEA to apply changes in plugins?",
        EnablePluginsNotificationAction::proposeRestart
    );
  }
}