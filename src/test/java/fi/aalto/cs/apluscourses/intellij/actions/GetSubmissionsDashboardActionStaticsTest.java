package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.SubmissionsDashboard;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SubmissionsDashboard.class)
public class GetSubmissionsDashboardActionStaticsTest {

  @Test
  public void testTryGetSubmissionsDashboardReturnsValid() throws IOException {
    //  given
    //  static mocks
    mockStatic(SubmissionsDashboard.class);

    //  regular mocks
    MainViewModelProvider mockMainViewModelProvider = mock(MainViewModelProvider.class);
    SubmissionsDashboard mockSubmissionsDashboard = mock(SubmissionsDashboard.class);
    when(SubmissionsDashboard.getSubmissionsDashboard(anyLong(), any(APlusAuthentication.class)))
        .thenReturn(mockSubmissionsDashboard);

    GetSubmissionsDashboardAction getSubmissionsDashboardAction = new GetSubmissionsDashboardAction(
        mockMainViewModelProvider, Notifications.Bus::notify);

    //  when
    SubmissionsDashboard submissionsDashboard = getSubmissionsDashboardAction
        .tryGetSubmissionsDashboard(1, mock(APlusAuthentication.class), mock(Project.class));

    //  then
    Assert.assertEquals("The returned object is the same as the reference one.",
        mockSubmissionsDashboard, submissionsDashboard);
  }

  @Test
  public void testTryGetSubmissionsDashboardHandlesExceptionProperly() throws IOException {
    //  given
    //  static mocks
    mockStatic(SubmissionsDashboard.class);

    //  regular mocks
    MainViewModelProvider mockMainViewModelProvider = mock(MainViewModelProvider.class);
    Notifier mockNotifier = mock(Notifier.class);
    IOException ioException = new IOException();
    when(SubmissionsDashboard.getSubmissionsDashboard(anyLong(), any(APlusAuthentication.class)))
        .thenThrow(ioException);

    GetSubmissionsDashboardAction getSubmissionsDashboardAction =
        new GetSubmissionsDashboardAction(mockMainViewModelProvider, mockNotifier);

    //  when
    SubmissionsDashboard submissionsDashboard = getSubmissionsDashboardAction
        .tryGetSubmissionsDashboard(1, mock(APlusAuthentication.class), mock(Project.class));

    //  then
    assertNull("The returned object is 'null'.", submissionsDashboard);
    verify(mockNotifier, times(1))
        .notify(any(NetworkErrorNotification.class), any(Project.class));
  }
}
