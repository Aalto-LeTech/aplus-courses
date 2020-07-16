package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightIdeaTestCase;

import fi.aalto.cs.apluscourses.intellij.model.FileFinder;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingFileNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NotSubmittableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.exercise.ModuleSelectionDialog;
import fi.aalto.cs.apluscourses.ui.exercise.SubmissionDialog;
import fi.aalto.cs.apluscourses.utils.observable.CompoundObservableProperty;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

public class SubmitExerciseActionTest extends LightIdeaTestCase {

  private AnActionEvent actionEvent;

  private Module module;

  private MainViewModelProvider mainViewModelProvider;

  private SubmitExerciseAction.SubmissionInfoFactory submissionInfoFactory;
  private AtomicInteger submissionInfoCallCount = new AtomicInteger(0);

  private SubmitExerciseAction.SubmissionHistoryFactory submissionHistoryFactory;
  private AtomicInteger submissionHistoryCallCount = new AtomicInteger(0);

  private SubmitExerciseAction.GroupsFactory groupsFactory;
  private AtomicInteger groupsCallCount = new AtomicInteger(0);

  private FileFinder fileFinder;

  private ModuleSelectionDialog.Factory moduleDialogFactory;

  private SubmissionDialog.Factory submissionDialogFactory;
  private AtomicBoolean submissionDialogIsShown;

  private TestNotifier notifier;

  private Submission.Submitter submitter;

  private final Group selfGroup = new Group(0, Collections.singletonList("Submit alone"));

  class TestModuleSelectionDialog extends ModuleSelectionDialog {

    private ModuleSelectionViewModel viewModel;
    private boolean ok;

    public TestModuleSelectionDialog(ModuleSelectionViewModel viewModel,
                                     Project project,
                                     boolean ok) {
      super(viewModel, project);
      this.viewModel = viewModel;
      this.ok = ok;
    }

    @Override
    public boolean showAndGet() {
      viewModel.selectedModule.set(module);
      return ok;
    }
  }

  class TestSubmissionDialog extends SubmissionDialog {

    public TestSubmissionDialog(SubmissionViewModel viewModel, Project project) {
      super(viewModel, project);
    }

    @Override
    public boolean showAndGet() {
      getViewModel().selectedGroup.set(selfGroup);
      submissionDialogIsShown.set(true);
      return true;
    }

  }

  class TestNotifier implements Notifier {
    private Notification notification;

    @Override
    public void notify(@NotNull Notification notification, @Nullable Project project) {
      this.notification = notification;
    }

    public Notification getNotification() {
      return notification;
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    actionEvent = mock(AnActionEvent.class);
    Project project = mock(Project.class);
    doReturn(project).when(actionEvent).getProject();

    module = mock(Module.class);
    doReturn(Paths.get("parent", "child").toString())
        .when(module).getModuleFilePath();
    doReturn("name").when(module).getName();

    ExerciseViewModel exercise = new ExerciseViewModel(new Exercise(0, ""));
    ExercisesTreeViewModel exercisesViewModel = mock(ExercisesTreeViewModel.class);
    doReturn(exercise).when(exercisesViewModel).getSelectedExercise();

    MainViewModel mainViewModel = new MainViewModel();
    CourseViewModel courseViewModel = mock(CourseViewModel.class);
    doReturn(mock(Course.class)).when(courseViewModel).getModel();
    APlusAuthenticationViewModel authenticationViewModel = mock(APlusAuthenticationViewModel.class);
    doReturn(mock(APlusAuthentication.class)).when(authenticationViewModel).getAuthentication();
    ((CompoundObservableProperty) mainViewModel.exercisesViewModel).setConverter(
        (p1, p2) -> exercisesViewModel);
    mainViewModel.courseViewModel.set(courseViewModel);
    mainViewModel.authenticationViewModel.set(authenticationViewModel);

    mainViewModelProvider = p -> mainViewModel;

    submissionInfoFactory = (ex, auth) -> {
      submissionInfoCallCount.incrementAndGet();
      return new SubmissionInfo(10, Collections.singletonList(new SubmittableFile("file.scala")));
    };

    submissionHistoryFactory = (ex, auth) -> {
      submissionHistoryCallCount.incrementAndGet();
      return new SubmissionHistory(4);
    };

    groupsFactory = (course, auth) -> {
      groupsCallCount.incrementAndGet();
      return Collections.singletonList(selfGroup);
    };

    fileFinder = (directory, name) -> Paths.get("file.scala");

    moduleDialogFactory = (viewModel, proj) -> new TestModuleSelectionDialog(viewModel, proj, true);

    submissionDialogFactory = TestSubmissionDialog::new;
    submissionDialogIsShown = new AtomicBoolean(false);

    notifier = new TestNotifier();

    submitter = (url, authentication, data) -> { };
  }

  @Test
  public void testSubmitExerciseAction() {
    SubmitExerciseAction action = new SubmitExerciseAction(
        mainViewModelProvider,
        submissionInfoFactory,
        submissionHistoryFactory,
        groupsFactory,
        fileFinder,
        moduleDialogFactory,
        submissionDialogFactory,
        notifier,
        submitter
    );
    action.actionPerformed(actionEvent);

    Assert.assertEquals("The action fetches information necessary for submission",
        1, submissionInfoCallCount.get());
    Assert.assertEquals("The action fetches the submission history",
        1, submissionHistoryCallCount.get());
    Assert.assertEquals("The action fetches the available groups", 1, groupsCallCount.get());
  }

  @Test
  public void testNotifiesOfMissingFile() {
    SubmitExerciseAction action = new SubmitExerciseAction(
        mainViewModelProvider,
        submissionInfoFactory,
        submissionHistoryFactory,
        groupsFactory,
        (directory, filename) -> null,
        moduleDialogFactory,
        submissionDialogFactory,
        notifier,
        submitter
    );
    action.actionPerformed(actionEvent);

    Assert.assertFalse("The submission dialog is not shown", submissionDialogIsShown.get());
    Assert.assertTrue("A notification of the missing file is shown",
        notifier.getNotification() instanceof MissingFileNotification);
  }

  @Test
  public void testNotifiesExerciseNotSubmittable() {
    SubmitExerciseAction action = new SubmitExerciseAction(
        mainViewModelProvider,
        (exercise, authentication) -> new SubmissionInfo(10, Collections.emptyList()),
        submissionHistoryFactory,
        groupsFactory,
        fileFinder,
        moduleDialogFactory,
        submissionDialogFactory,
        notifier,
        submitter
    );
    action.actionPerformed(actionEvent);

    Assert.assertFalse("The submission dialog is not shown", submissionDialogIsShown.get());
    Assert.assertTrue("A notification of the error is shown",
        notifier.getNotification() instanceof NotSubmittableNotification);
  }

  @Test
  public void testNotifiesOfNetworkError1() {
    SubmitExerciseAction action = new SubmitExerciseAction(
        mainViewModelProvider,
        submissionInfoFactory,
        (exercise, authentication) -> {
          throw new IOException();
        },
        groupsFactory,
        fileFinder,
        moduleDialogFactory,
        submissionDialogFactory,
        notifier,
        submitter
    );
    action.actionPerformed(actionEvent);

    Assert.assertFalse("The submission dialog is not shown", submissionDialogIsShown.get());
    Assert.assertTrue("A network error notification is shown",
        notifier.getNotification() instanceof NetworkErrorNotification);
  }

  @Test
  public void testNotifiesOfNetworkError2() {
    SubmitExerciseAction action = new SubmitExerciseAction(
        mainViewModelProvider,
        submissionInfoFactory,
        submissionHistoryFactory,
        (course, authentication) -> {
          throw new IOException();
        },
        fileFinder,
        moduleDialogFactory,
        submissionDialogFactory,
        notifier,
        submitter
    );
    action.actionPerformed(actionEvent);

    Assert.assertFalse("The submission dialog is not shown", submissionDialogIsShown.get());
    Assert.assertTrue("A network error notification is shown",
        notifier.getNotification() instanceof NetworkErrorNotification);
  }

  @Test
  public void testModuleSelectionDialogCancel() {
    SubmitExerciseAction action = new SubmitExerciseAction(
        mainViewModelProvider,
        submissionInfoFactory,
        submissionHistoryFactory,
        groupsFactory,
        fileFinder,
        (viewModel, proj) -> new TestModuleSelectionDialog(viewModel, proj, false),
        submissionDialogFactory,
        notifier,
        submitter
    );
    action.actionPerformed(actionEvent);

    Assert.assertFalse("The submission dialog is not shown", submissionDialogIsShown.get());
    Assert.assertEquals("The submission information is fetched",
        1, submissionInfoCallCount.get());
    Assert.assertEquals("The submission history is not fetched",
        0, submissionHistoryCallCount.get());
    Assert.assertEquals("The groups are fetched", 1, groupsCallCount.get());
  }

}
