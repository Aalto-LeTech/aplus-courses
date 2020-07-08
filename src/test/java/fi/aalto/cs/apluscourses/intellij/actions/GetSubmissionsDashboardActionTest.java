package fi.aalto.cs.apluscourses.intellij.actions;

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
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import org.junit.Test;

public class GetSubmissionsDashboardActionTest extends BasePlatformTestCase {

  @Test
  public void testActionPerformed() {
    Project project = getProject();
    AnActionEvent anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();

    MainViewModelProvider mockMainViewModelProvider = mock(MainViewModelProvider.class);
    MainViewModel mockMainViewModel = mock(MainViewModel.class);
    @SuppressWarnings("unchecked")
    ObservableProperty<APlusAuthenticationViewModel> mockAuthenticationViewModelObservableProperty =
        (ObservableProperty<APlusAuthenticationViewModel>) mock(ObservableProperty.class);
    APlusAuthenticationViewModel mockAuthenticationViewModel =
        mock(APlusAuthenticationViewModel.class);
    APlusAuthentication mockAuthentication = mock(APlusAuthentication.class);

    @SuppressWarnings("unchecked")
    ObservableProperty<CourseViewModel> mockCourseViewModelObservableProperty =
        (ObservableProperty<CourseViewModel>) mock(ObservableProperty.class);
    CourseViewModel mockCourseViewModel = mock(CourseViewModel.class);
    Course mockCourse = mock(Course.class);

    when(mockMainViewModelProvider.getMainViewModel(project)).thenReturn(mockMainViewModel);
    when(mockMainViewModel.getAuthenticationViewModel())
        .thenReturn(mockAuthenticationViewModelObservableProperty);
    when(mockAuthenticationViewModelObservableProperty.get())
        .thenReturn(mockAuthenticationViewModel);
    when(mockAuthenticationViewModel.getAuthentication()).thenReturn(mockAuthentication);

    when(mockMainViewModel.getCourseViewModel())
        .thenReturn(mockCourseViewModelObservableProperty);
    when(mockCourseViewModelObservableProperty.get())
        .thenReturn(mockCourseViewModel);
    when(mockCourseViewModel.getModel()).thenReturn(mockCourse);
    when(mockCourse.getId()).thenReturn("1");

    GetSubmissionsDashboardAction getSubmissionsDashboardAction = new GetSubmissionsDashboardAction(
        mockMainViewModelProvider, Notifications.Bus::notify);
    GetSubmissionsDashboardAction spyGetSubmissionsDashboardAction = spy(
        getSubmissionsDashboardAction);

    SubmissionsDashboard mockSubmissionsDashboard = mock(SubmissionsDashboard.class);
    doReturn(mockSubmissionsDashboard).when(spyGetSubmissionsDashboardAction)
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);

    spyGetSubmissionsDashboardAction.actionPerformed(anActionEvent);

    verify(spyGetSubmissionsDashboardAction, times(1))
        .tryGetSubmissionsDashboard(1L, mockAuthentication, project);
    verify(mockCourse, times(1)).setSubmissionsDashboard(mockSubmissionsDashboard);
  }

  @Test
  public void testTryGetSubmissionsDashboard() {
  }
}