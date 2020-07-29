package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionRenderingErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OpenSubmissionActionTest {

  private SubmissionResult submissionResult;
  private AnActionEvent actionEvent;
  private MainViewModelProvider mainViewModelProvider;
  private Notifier notifier;
  private OpenSubmissionAction.SubmissionRenderer submissionRenderer;

  /**
   * Called before each test.
   */
  @Before
  public void setUp() {
    submissionResult = new SubmissionResult(1, 1, "http://example.com");
    SubmissionResultViewModel viewModel = new SubmissionResultViewModel(submissionResult);

    ExercisesTreeViewModel exercisesTree = mock(ExercisesTreeViewModel.class);
    doReturn(viewModel).when(exercisesTree).getSelectedSubmission();

    MainViewModel mainViewModel = new MainViewModel();
    mainViewModel.exercisesViewModel.set(exercisesTree);

    mainViewModelProvider = project -> mainViewModel;

    notifier = mock(Notifier.class);
    submissionRenderer = mock(OpenSubmissionAction.SubmissionRenderer.class);
    actionEvent = mock(AnActionEvent.class);
  }

  @Test
  public void testOpenSubmissionAction() throws Exception {
    OpenSubmissionAction action = new OpenSubmissionAction(
        mainViewModelProvider,
        notifier,
        submissionRenderer
    );
    action.actionPerformed(actionEvent);

    ArgumentCaptor<SubmissionResult> argumentCaptor
        = ArgumentCaptor.forClass(SubmissionResult.class);
    verify(submissionRenderer).show(argumentCaptor.capture());
    assertSame(submissionResult, argumentCaptor.getValue());
  }

  @Test
  public void testErrorNotification() {
    Exception exception = new Exception();
    OpenSubmissionAction action = new OpenSubmissionAction(
        mainViewModelProvider,
        notifier,
        submission -> {
          throw exception;
        }
    );
    action.actionPerformed(actionEvent);

    ArgumentCaptor<SubmissionRenderingErrorNotification> argumentCaptor
        = ArgumentCaptor.forClass(SubmissionRenderingErrorNotification.class);
    verify(notifier).notify(argumentCaptor.capture(), any(Project.class));
    assertSame(exception, argumentCaptor.getValue().getException());
  }

}
