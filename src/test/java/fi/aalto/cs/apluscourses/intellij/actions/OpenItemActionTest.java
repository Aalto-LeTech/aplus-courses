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
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.Collections;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OpenItemActionTest {

  private final String submissionString = "submission";
  private final String exerciseString = "exercise";
  private final String weekString = "week";
  private Exercise exercise;
  private SubmissionResult submissionResult;
  private ExerciseGroup exerciseGroup;
  private AnActionEvent actionEvent;
  private MainViewModelProvider mainViewModelProvider;
  private Notifier notifier;
  private UrlRenderer urlRenderer;

  /**
   * Called before each test.
   * @param viewModelType The type of SelectableNodeViewModel to be tested, as a string
   */
  public void setUp(String viewModelType) {
    exercise = new Exercise(223, "TestEx", "http://example.com", 0, 1, 10, true);
    submissionResult
        = new SubmissionResult(1, 0, SubmissionResult.Status.GRADED, exercise);
    exerciseGroup = new ExerciseGroup(0, "", "https://url.com/", Collections.emptyList());

    SubmissionResultViewModel submissionResultViewModel
        = new SubmissionResultViewModel(submissionResult, 1);
    ExerciseViewModel exerciseViewModel = new ExerciseViewModel(exercise);
    ExerciseGroupViewModel exerciseGroupViewModel = new ExerciseGroupViewModel(exerciseGroup);

    SelectableNodeViewModel<?> selectableNodeViewModel;
    if (viewModelType.equals(submissionString)) {
      selectableNodeViewModel = submissionResultViewModel;
    } else if (viewModelType.equals(exerciseString)) {
      selectableNodeViewModel = exerciseViewModel;
    } else {
      selectableNodeViewModel = exerciseGroupViewModel;
    }

    ExercisesTreeViewModel exercisesTree = mock(ExercisesTreeViewModel.class);
    doReturn(selectableNodeViewModel).when(exercisesTree).getSelectedItem();

    MainViewModel mainViewModel = new MainViewModel(new Options());
    mainViewModel.exercisesViewModel.set(exercisesTree);

    mainViewModelProvider = project -> mainViewModel;

    notifier = mock(Notifier.class);
    urlRenderer = mock(UrlRenderer.class);
    actionEvent = mock(AnActionEvent.class);
    doReturn(mock(Project.class)).when(actionEvent).getProject();
  }

  @Test
  public void testOpenItemActionSubmission() throws Exception {
    setUp(submissionString);
    OpenItemAction action = new OpenItemAction(
        mainViewModelProvider,
        urlRenderer,
        notifier
    );
    action.actionPerformed(actionEvent);

    ArgumentCaptor<String> argumentCaptor
        = ArgumentCaptor.forClass(String.class);
    verify(urlRenderer).show(argumentCaptor.capture());
    assertEquals(submissionResult.getHtmlUrl(), argumentCaptor.getValue());
  }

  @Test
  public void testOpenItemActionExercise() throws Exception {
    setUp(exerciseString);
    OpenItemAction action = new OpenItemAction(
        mainViewModelProvider,
        urlRenderer,
        notifier
    );
    action.actionPerformed(actionEvent);

    ArgumentCaptor<String> argumentCaptor
        = ArgumentCaptor.forClass(String.class);
    verify(urlRenderer).show(argumentCaptor.capture());
    assertEquals(exercise.getHtmlUrl(), argumentCaptor.getValue());
  }

  @Test
  public void testOpenItemActionWeek() throws Exception {
    setUp(weekString);
    OpenItemAction action = new OpenItemAction(
        mainViewModelProvider,
        urlRenderer,
        notifier
    );
    action.actionPerformed(actionEvent);

    ArgumentCaptor<String> argumentCaptor
        = ArgumentCaptor.forClass(String.class);
    verify(urlRenderer).show(argumentCaptor.capture());
    assertEquals(exerciseGroup.getHtmlUrl(), argumentCaptor.getValue());
  }

  @Test
  public void testErrorNotification() throws Exception {
    setUp(submissionString);
    Exception exception = new Exception();
    doThrow(exception).when(urlRenderer).show(anyString());
    OpenItemAction action = new OpenItemAction(
        mainViewModelProvider,
        urlRenderer,
        notifier
    );
    action.actionPerformed(actionEvent);

    ArgumentCaptor<SubmissionRenderingErrorNotification> argumentCaptor
        = ArgumentCaptor.forClass(SubmissionRenderingErrorNotification.class);
    verify(notifier).notify(argumentCaptor.capture(), any(Project.class));
    assertSame(exception, argumentCaptor.getValue().getException());
  }

}
