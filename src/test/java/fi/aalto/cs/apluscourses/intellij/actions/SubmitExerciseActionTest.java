package fi.aalto.cs.apluscourses.intellij.actions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.DialogHelper;
import fi.aalto.cs.apluscourses.intellij.actions.SubmitExerciseAction.Tagger;
import fi.aalto.cs.apluscourses.intellij.notifications.ExerciseNotSelectedNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingFileNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingModuleNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NotSubmittableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionSentNotification;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.FileDoesNotExistException;
import fi.aalto.cs.apluscourses.model.FileFinder;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SubmitExerciseActionTest {

  Course course;
  long exerciseId;
  Exercise exercise;
  Group group;
  List<Group> groups;
  ExerciseGroup exerciseGroup;
  List<ExerciseGroup> exerciseGroups;
  String fileKey;
  String fileName;
  SubmittableFile file;
  SubmissionInfo submissionInfo;
  SubmissionHistory submissionHistory;
  MainViewModel mainViewModel;
  CourseViewModel courseViewModel;
  String token;
  Authentication authentication;
  ExerciseDataSource exerciseDataSource;
  ExercisesTreeViewModel exercises;
  String moduleName;
  Path modulePath;
  String moduleFilePath;
  Module module;
  Project project;
  Path filePath;
  DialogHelper<ModuleSelectionViewModel> moduleSelectionDialog;
  DialogHelper<SubmissionViewModel> submissionDialog;
  Dialogs.Factory<ModuleSelectionViewModel> moduleSelectionDialogFactory;
  Dialogs.Factory<SubmissionViewModel> submissionDialogFactory;
  MainViewModelProvider mainVmProvider;
  FileFinder fileFinder;
  SubmitExerciseAction.ModuleSource moduleSource;
  Dialogs dialogs;
  Notifier notifier;
  AnActionEvent event;
  SubmitExerciseAction action;
  Points points;
  Tagger tagger;

  /**
   * Called before each test.
   *
   * @throws IOException               Never.
   * @throws FileDoesNotExistException Never.
   */
  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws IOException, FileDoesNotExistException {
    exerciseId = 12;
    exercise = new Exercise(exerciseId, "Test exercise", "http://localhost:10000",
        Collections.emptyList(), 0, 0, 0);
    group = new Group(124, Collections.singletonList("Only you"));
    groups = Collections.singletonList(group);
    exerciseGroup = new ExerciseGroup("Test EG", Collections.singletonList(exercise));
    exerciseGroups = Collections.singletonList(exerciseGroup);
    fileName = "some_file.scala";
    fileKey = "file1";
    file = new SubmittableFile(fileKey, fileName);
    String language = "fi";
    submissionInfo = new SubmissionInfo(
        1, Collections.singletonMap(language, Collections.singletonList(file)));
    submissionHistory = new SubmissionHistory(0);

    mainViewModel = new MainViewModel();

    authentication = mock(Authentication.class);
    points = new Points(Collections.emptyMap(), Collections.emptyMap());

    exerciseDataSource = mock(ExerciseDataSource.class);
    course = spy(new ModelExtensions.TestCourse("91", "NineOne Course", exerciseDataSource));
    doReturn(groups).when(exerciseDataSource).getGroups(course, authentication);
    doReturn(submissionInfo).when(exerciseDataSource).getSubmissionInfo(exercise, authentication);
    doReturn(submissionHistory).when(exerciseDataSource)
        .getSubmissionHistory(exercise, authentication);
    doReturn(points).when(exerciseDataSource).getPoints(course, authentication);
    doReturn(exerciseGroups).when(exerciseDataSource)
        .getExerciseGroups(course, points, authentication);
    doReturn("http://localhost:1000")
        .when(exerciseDataSource)
        .submit(any(Submission.class), any(Authentication.class));

    courseViewModel = new CourseViewModel(course);
    mainViewModel.courseViewModel.set(courseViewModel);

    mainViewModel.authentication.set(authentication);

    exercises = Objects.requireNonNull(mainViewModel.exercisesViewModel.get());
    exercises.getGroupViewModels().get(0).getExerciseViewModels().get(0).setSelected(true);

    moduleName = "MyModule";
    modulePath = Paths.get(moduleName);
    moduleFilePath = modulePath.resolve("MyModule.iml").toString();
    module = mock(Module.class);
    // Needed because SubmitExerciseAction uses ModuleUtilCore#getModuleDirPath
    doReturn(moduleFilePath).when(module).getModuleFilePath();
    doReturn(moduleName).when(module).getName();

    project = mock(Project.class);
    doReturn(FileUtilRt.getTempDirectory()).when(project).getBasePath();

    PluginSettings
        .getInstance()
        .getCourseFileManager(project)
        .createAndLoad(new URL("http://localhost:8000"), language);

    filePath = modulePath.resolve(fileName);

    mainVmProvider = mock(MainViewModelProvider.class);
    doReturn(mainViewModel).when(mainVmProvider).getMainViewModel(project);

    fileFinder = mock(FileFinder.class);
    doReturn(filePath).when(fileFinder).tryFindFile(modulePath, fileName);
    doCallRealMethod().when(fileFinder).findFile(any(), any());
    doCallRealMethod().when(fileFinder).findFiles(any(), any());

    moduleSource = mock(SubmitExerciseAction.ModuleSource.class);
    doReturn(new Module[]{module}).when(moduleSource).getModules(project);
    doReturn(module).when(moduleSource).getModule(project, moduleName);

    dialogs = new Dialogs();

    notifier = mock(Notifier.class);

    event = mock(AnActionEvent.class);
    doReturn(project).when(event).getProject();

    moduleSelectionDialog = spy(new DialogHelper<>(viewModel -> {
      viewModel.selectedModule.set(viewModel.getModules()[0]);
      return true;
    }));
    moduleSelectionDialogFactory = new DialogHelper.Factory<>(moduleSelectionDialog, project);
    dialogs.register(ModuleSelectionViewModel.class, moduleSelectionDialogFactory);

    submissionDialog = spy(new DialogHelper<>(viewModel -> {
      // we select the second group of the list (the first is "submit alone")
      viewModel.selectedGroup.set(viewModel.getAvailableGroups().get(1));
      return true;
    }));
    submissionDialogFactory = new DialogHelper.Factory<>(submissionDialog, project);
    dialogs.register(SubmissionViewModel.class, submissionDialogFactory);

    tagger = mock(Tagger.class);

    action = new SubmitExerciseAction(mainVmProvider, fileFinder, moduleSource, dialogs, notifier,
        tagger);
  }

  @Test
  public void testSubmitExerciseAction() throws IOException {
    action.actionPerformed(event);

    ArgumentCaptor<Submission> submissionArg = ArgumentCaptor.forClass(Submission.class);
    verify(exerciseDataSource).submit(submissionArg.capture(), same(authentication));

    Submission submission = submissionArg.getValue();
    assertEquals(group, submission.getGroup());
    assertEquals(exercise, submission.getExercise());

    Map<String, Path> files = new HashMap<>();
    files.put(fileKey, filePath);
    assertThat(submission.getFiles(), is(files));

    ArgumentCaptor<SubmissionSentNotification> notificationArg =
        ArgumentCaptor.forClass(SubmissionSentNotification.class);

    verify(notifier).notify(notificationArg.capture(), eq(project));
  }

  @Test
  public void testNotifiesNoExerciseSelected() {
    exercises.getGroupViewModels().get(0).getExerciseViewModels().get(0).setSelected(false);

    action.actionPerformed(event);

    ArgumentCaptor<ExerciseNotSelectedNotification> notification
        = ArgumentCaptor.forClass(ExerciseNotSelectedNotification.class);

    verify(notifier).notify(notification.capture(), eq(project));
  }

  @Test
  public void testNotifiesOfMissingFile() throws IOException {
    doReturn(null).when(fileFinder).tryFindFile(modulePath, fileName);

    action.actionPerformed(event);

    verifyNoInteractions(submissionDialog);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<MissingFileNotification> notificationArg =
        ArgumentCaptor.forClass(MissingFileNotification.class);

    verify(notifier).notify(notificationArg.capture(), eq(project));

    MissingFileNotification notification = notificationArg.getValue();
    assertEquals(fileName, notification.getFilename());
    assertEquals(modulePath, notification.getPath());

    verifyNoMoreInteractions(notifier);
  }

  @Test
  public void testNotifiesOfMissingModule() throws IOException {
    String nonexistentModuleName = "nonexistent module";

    Map<Long, Map<String, String>> map = Collections.singletonMap(exerciseId,
        Collections.singletonMap("fi", nonexistentModuleName));
    doReturn(map).when(course).getExerciseModules();

    action.actionPerformed(event);

    verifyNoInteractions(moduleSelectionDialog);
    verifyNoInteractions(submissionDialog);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<MissingModuleNotification> notificationArg =
        ArgumentCaptor.forClass(MissingModuleNotification.class);

    verify(notifier).notify(notificationArg.capture(), eq(project));

    MissingModuleNotification notification = notificationArg.getValue();
    assertEquals(nonexistentModuleName, notification.getModuleName());

    verifyNoMoreInteractions(notifier);
  }

  @Test
  public void testNotifiesExerciseNotSubmittable() throws IOException {
    doReturn(new SubmissionInfo(1, Collections.emptyMap()))
        .when(exerciseDataSource)
        .getSubmissionInfo(exercise, authentication);

    action.actionPerformed(event);

    verifyNoInteractions(moduleSelectionDialog);
    verifyNoInteractions(submissionDialog);
    verify(exerciseDataSource, never()).submit(any(), any());

    verify(notifier).notify(any(NotSubmittableNotification.class), eq(project));

    verifyNoMoreInteractions(notifier);
  }

  @Test
  public void testNotifiesOfNetworkErrorWhenGettingSubmissionHistory() throws IOException {
    IOException exception = new IOException();
    doThrow(exception).when(exerciseDataSource).getSubmissionHistory(exercise, authentication);

    action.actionPerformed(event);

    verifyNoInteractions(submissionDialog);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<NetworkErrorNotification> notificationArg =
        ArgumentCaptor.forClass(NetworkErrorNotification.class);

    verify(notifier).notify(notificationArg.capture(), eq(project));

    NetworkErrorNotification notification = notificationArg.getValue();
    assertSame(exception, notification.getException());

    verifyNoMoreInteractions(notifier);
  }

  @Test
  public void testNotifiesOfNetworkErrorWhenGettingGroups() throws IOException {
    IOException exception = new IOException();
    doThrow(exception).when(exerciseDataSource).getGroups(course, authentication);

    action.actionPerformed(event);

    verifyNoInteractions(submissionDialog);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<NetworkErrorNotification> notificationArg =
        ArgumentCaptor.forClass(NetworkErrorNotification.class);

    verify(notifier).notify(notificationArg.capture(), eq(project));

    NetworkErrorNotification notification = notificationArg.getValue();
    assertSame(exception, notification.getException());

    verifyNoMoreInteractions(notifier);
  }

  @Test
  public void testNotifiesOfNetworkErrorWhenGettingSubmissionInfo() throws IOException {
    IOException exception = new IOException();
    doThrow(exception).when(exerciseDataSource).getSubmissionInfo(exercise, authentication);

    action.actionPerformed(event);

    verifyNoInteractions(moduleSelectionDialog);
    verifyNoInteractions(submissionDialog);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<NetworkErrorNotification> notificationArg =
        ArgumentCaptor.forClass(NetworkErrorNotification.class);

    verify(notifier).notify(notificationArg.capture(), eq(project));

    NetworkErrorNotification notification = notificationArg.getValue();
    assertSame(exception, notification.getException());

    verifyNoMoreInteractions(notifier);
  }

  @Test
  public void testNotifiesOfNetworkErrorWhenSubmitting() throws IOException {
    IOException exception = new IOException();
    doThrow(exception).when(exerciseDataSource).submit(any(), any());

    action.actionPerformed(event);

    ArgumentCaptor<NetworkErrorNotification> notificationArg =
        ArgumentCaptor.forClass(NetworkErrorNotification.class);

    verify(notifier).notify(notificationArg.capture(), eq(project));

    NetworkErrorNotification notification = notificationArg.getValue();
    assertSame(exception, notification.getException());

    verifyNoMoreInteractions(notifier);
  }

  @Test
  public void testModuleSelectionDialogCancel() throws IOException {
    dialogs.register(SubmissionViewModel.class, (submission, project) -> () -> false);

    action.actionPerformed(event);

    verify(exerciseDataSource, never()).submit(any(), any());

    verifyNoInteractions(notifier);
  }
}
