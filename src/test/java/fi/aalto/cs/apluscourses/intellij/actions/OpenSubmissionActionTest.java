package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionRenderingErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OpenSubmissionActionTest {

  private SubmissionResult submissionResult;
  private AnActionEvent actionEvent;
  private MainViewModelProvider mainViewModelProvider;
  private Notifier notifier;
  private UrlRenderer submissionRenderer;

  /**
   * Called before each test.
   */
  @Before
  public void setUp() {
    Exercise exercise = new Exercise(223, "TestEx", "http://example.com", 0, 1, 10);
    submissionResult
        = new SubmissionResult(1, 0, SubmissionResult.Status.GRADED, exercise);
    SubmissionResultViewModel viewModel
        = new SubmissionResultViewModel(submissionResult, 1);

    ExercisesTreeViewModel exercisesTree = mock(ExercisesTreeViewModel.class);
    BaseTreeViewModel.Selection selection = new BaseTreeViewModel.Selection(
            exercisesTree,
            mock(ExerciseGroupViewModel.class),
            new ExerciseViewModel(exercise),
            viewModel
        );
    doReturn(selection).when(exercisesTree).findSelected();

    MainViewModel mainViewModel = new MainViewModel(new Options());
    mainViewModel.exercisesViewModel.set(exercisesTree);

    mainViewModelProvider = project -> mainViewModel;

    notifier = mock(Notifier.class);
    submissionRenderer = mock(UrlRenderer.class);
    actionEvent = mock(AnActionEvent.class);
    doReturn(mock(Project.class)).when(actionEvent).getProject();
  }

  @Test
  public void testOpenSubmissionAction() throws Exception {
    OpenSubmissionAction action = new OpenSubmissionAction(
        mainViewModelProvider,
        submissionRenderer,
        notifier
    );
    action.actionPerformed(actionEvent);

    ArgumentCaptor<String> argumentCaptor
        = ArgumentCaptor.forClass(String.class);
    verify(submissionRenderer).show(argumentCaptor.capture());
    assertEquals(submissionResult.getUrl(), argumentCaptor.getValue());
  }

  @Test
  public void testErrorNotification() throws Exception {
    Exception exception = new Exception();
    doThrow(exception).when(submissionRenderer).show(anyString());
    OpenSubmissionAction action = new OpenSubmissionAction(
        mainViewModelProvider,
        submissionRenderer,
        notifier
    );
    action.actionPerformed(actionEvent);

    ArgumentCaptor<SubmissionRenderingErrorNotification> argumentCaptor
        = ArgumentCaptor.forClass(SubmissionRenderingErrorNotification.class);
    verify(notifier).notify(argumentCaptor.capture(), any(Project.class));
    assertSame(exception, argumentCaptor.getValue().getException());
  }

}
