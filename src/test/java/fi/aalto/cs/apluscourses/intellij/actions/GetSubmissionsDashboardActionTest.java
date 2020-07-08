package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
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

    when(mockMainViewModelProvider.getMainViewModel(project)).thenReturn(mockMainViewModel);
    when(mockMainViewModel.getAuthenticationViewModel())
        .thenReturn(mockAuthenticationViewModelObservableProperty);
    when(mockAuthenticationViewModelObservableProperty.get())
        .thenReturn(mockAuthenticationViewModel);
    when(mockAuthenticationViewModel.getAuthentication()).thenReturn(mockAuthentication);

    new GetSubmissionsDashboardAction(mockMainViewModelProvider, Notifications.Bus::notify)
        .actionPerformed(anActionEvent);
  }

  @Test
  public void testTryGetSubmissionsDashboard() {
  }
}