package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.UrlRenderingErrorNotification;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import java.net.URISyntaxException;
import java.util.OptionalLong;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OpenSubmissionNotificationActionTest {

  private AnActionEvent event;
  private SubmissionResult submissionResult;
  private Notification notification;
  private Notifier notifier;
  private UrlRenderer submissionRenderer;

  /**
   * Called before each test.
   */
  @Before
  public void setUp() {
    event = mock(AnActionEvent.class);
    doReturn(mock(Project.class)).when(event).getProject();
    submissionResult = new SubmissionResult(1, 0, 0.0, SubmissionResult.Status.GRADED,
        new Exercise(1, "Ex", "http://example.com", 0, 5, 10, true, OptionalLong.empty()));
    notification = mock(Notification.class);
    notifier = mock(Notifier.class);
    submissionRenderer = mock(UrlRenderer.class);
  }

  @Test
  public void testOpenSubmissionNotificationAction() throws Exception {
    new OpenSubmissionNotificationAction(
        submissionResult,
        submissionRenderer,
        notifier
    ).actionPerformed(event, notification);

    ArgumentCaptor<String> argumentCaptor
        = ArgumentCaptor.forClass(String.class);
    verify(submissionRenderer).show(argumentCaptor.capture());
    assertEquals(submissionResult.getHtmlUrl(), argumentCaptor.getValue());
  }

  @Test
  public void testErrorNotification() throws URISyntaxException {
    URISyntaxException exception = new URISyntaxException("input", "reason");
    doThrow(exception).when(submissionRenderer).show(anyString());

    new OpenSubmissionNotificationAction(
        submissionResult,
        submissionRenderer,
        notifier
    ).actionPerformed(event, notification);

    ArgumentCaptor<UrlRenderingErrorNotification> argumentCaptor
        = ArgumentCaptor.forClass(UrlRenderingErrorNotification.class);
    verify(notifier).notify(argumentCaptor.capture(), any(Project.class));
    assertSame(exception, argumentCaptor.getValue().getException());
  }

}
