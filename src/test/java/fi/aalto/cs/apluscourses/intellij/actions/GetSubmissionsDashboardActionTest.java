package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.SubmissionsDashboard;
import org.junit.Test;

public class GetSubmissionsDashboardActionTest extends BasePlatformTestCase {

  @Test
  public void testActionPerformedWithValidDataWorks() {
    //  given
    Project project = getProject();
    AnActionEvent anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();

    MainViewModelProvider mockMainViewModelProvider = mock(MainViewModelProvider.class,
        RETURNS_DEEP_STUBS);
    //  auth
    APlusAuthentication mockAuthentication = mock(APlusAuthentication.class);
    when(mockMainViewModelProvider.getMainViewModel(project).getAuthenticationViewModel().get()
        .getAuthentication()).thenReturn(mockAuthentication);

    //  course
    Course mockCourse = mock(Course.class);
    when(mockMainViewModelProvider.getMainViewModel(project).getCourseViewModel().get().getModel())
        .thenReturn(mockCourse);
    when(mockCourse.getId()).thenReturn("1");

    //  action
    GetSubmissionsDashboardAction getSubmissionsDashboardAction = new GetSubmissionsDashboardAction(
        mockMainViewModelProvider, Notifications.Bus::notify);
    GetSubmissionsDashboardAction spyGetSubmissionsDashboardAction = spy(
        getSubmissionsDashboardAction);

    //  submissions
    SubmissionsDashboard mockSubmissionsDashboard = mock(SubmissionsDashboard.class);
    doReturn(mockSubmissionsDashboard).when(spyGetSubmissionsDashboardAction)
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);

    //  when
    spyGetSubmissionsDashboardAction.actionPerformed(anActionEvent);

    //  then
    verify(spyGetSubmissionsDashboardAction, times(1))
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);
    verify(mockCourse, times(1))
        .setSubmissionsDashboard(mockSubmissionsDashboard);
  }

  @Test
  public void testActionPerformedWithInValidDataFails() {
    //  given
    Project project = getProject();
    AnActionEvent anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();

    MainViewModelProvider mockMainViewModelProvider = mock(MainViewModelProvider.class,
        RETURNS_DEEP_STUBS);
    //  auth
    APlusAuthentication mockAuthentication = null;
    when(mockMainViewModelProvider.getMainViewModel(project).getAuthenticationViewModel().get()
        .getAuthentication()).thenReturn(mockAuthentication);

    //  course
    Course mockCourse = mock(Course.class);
    when(mockMainViewModelProvider.getMainViewModel(project).getCourseViewModel().get().getModel())
        .thenReturn(mockCourse);
    when(mockCourse.getId()).thenReturn("1");

    //  action
    GetSubmissionsDashboardAction getSubmissionsDashboardAction = new GetSubmissionsDashboardAction(
        mockMainViewModelProvider, Notifications.Bus::notify);
    GetSubmissionsDashboardAction spyGetSubmissionsDashboardAction = spy(
        getSubmissionsDashboardAction);

    //  submissions
    SubmissionsDashboard mockSubmissionsDashboard = mock(SubmissionsDashboard.class);
    doReturn(mockSubmissionsDashboard).when(spyGetSubmissionsDashboardAction)
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);

    //  when
    spyGetSubmissionsDashboardAction.actionPerformed(anActionEvent);

    //  then
    verify(spyGetSubmissionsDashboardAction, times(0))
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);
    verify(mockCourse, times(0))
        .setSubmissionsDashboard(mockSubmissionsDashboard);
  }
}
