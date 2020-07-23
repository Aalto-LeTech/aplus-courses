package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.SubmissionResultsList;
import org.junit.Test;

public class GetSubmissionResultsListActionTest extends BasePlatformTestCase {

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
    when(mockMainViewModelProvider.getMainViewModel(project).getExerciseDataSource()
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
    SubmissionResultsList mockSubmissionResultsList = mock(SubmissionResultsList.class);
    doReturn(mockSubmissionResultsList).when(spyGetSubmissionsDashboardAction)
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);

    //  when
    spyGetSubmissionsDashboardAction.actionPerformed(anActionEvent);

    //  then
    verify(spyGetSubmissionsDashboardAction)
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);
    verify(mockCourse).setSubmissionsDashboard(mockSubmissionResultsList);
  }

  @Test
  public void testActionPerformedWithInvalidDataFails() {
    //  given
    Project project = getProject();
    AnActionEvent anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();

    MainViewModelProvider mockMainViewModelProvider = mock(MainViewModelProvider.class,
        RETURNS_DEEP_STUBS);
    //  exerciseDataSource (auth)
    APlusAuthentication mockAuthentication = null;
    when(mockMainViewModelProvider.getMainViewModel(project).getExerciseDataSource())
        .thenReturn(null);

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
    SubmissionResultsList mockSubmissionResultsList = mock(SubmissionResultsList.class);
    doReturn(mockSubmissionResultsList).when(spyGetSubmissionsDashboardAction)
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);

    //  when
    spyGetSubmissionsDashboardAction.actionPerformed(anActionEvent);

    //  then
    verify(spyGetSubmissionsDashboardAction, never())
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);
    verify(mockCourse, never())
        .setSubmissionsDashboard(mockSubmissionResultsList);
  }
}
