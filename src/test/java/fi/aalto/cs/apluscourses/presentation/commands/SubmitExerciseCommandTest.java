package fi.aalto.cs.apluscourses.presentation.commands;

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
import static org.mockito.Mockito.when;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
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
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.dialogs.TestDialogs;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.presentation.messages.Messenger;
import fi.aalto.cs.apluscourses.presentation.messages.MissingFileMessage;
import fi.aalto.cs.apluscourses.presentation.messages.MissingModuleMessage;
import fi.aalto.cs.apluscourses.presentation.messages.NetworkErrorMessage;
import fi.aalto.cs.apluscourses.presentation.messages.NotSubmittableMessage;
import java.io.IOException;
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

public class SubmitExerciseCommandTest {

  Authentication authentication;
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
  MainViewModel mainViewModel;
  CourseViewModel courseViewModel;
  ExerciseDataSource exerciseDataSource;
  ExercisesTreeViewModel exercises;
  String moduleName;
  Path modulePath;
  String moduleFilePath;
  Module module;
  Path filePath;
  TestDialogs.FactoryImpl<ModuleSelectionViewModel> moduleSelectionDialogFactory;
  TestDialogs.FactoryImpl<SubmissionViewModel> submissionDialogFactory;
  FileFinder fileFinder;
  SubmitExerciseCommand.ModuleSource moduleSource;
  TestDialogs dialogs;
  Messenger messenger;
  SubmitExerciseCommand.Context context;
  SubmitExerciseCommand command;

  /**
   * Called before each test.
   *
   * @throws IOException Never.
   * @throws FileDoesNotExistException Never.
   */
  @SuppressWarnings("unchecked")
  @Before
  public void setUp()
      throws IOException, FileDoesNotExistException,SubmitExerciseCommand.ModuleMissingException {
    exerciseDataSource = spy(new ModelExtensions.TestExerciseDataSource());
    course = spy(new ModelExtensions.TestCourse("91", "nineone", exerciseDataSource));
    exerciseId = 12;
    exercise = new Exercise(exerciseId, "Test exercise", Collections.emptyList(), 0, 0, 0);
    group = new Group(124, Collections.singletonList("Only you"));
    groups = Collections.singletonList(group);
    exerciseGroup = new ExerciseGroup("Test EG", Collections.singletonList(exercise));
    exerciseGroups = Collections.singletonList(exerciseGroup);
    fileName = "some_file.scala";
    fileKey = "file1";
    file = new SubmittableFile(fileKey, fileName);
    submissionInfo = new SubmissionInfo(1, new SubmittableFile[]{file});

    mainViewModel = new MainViewModel();

    courseViewModel = new CourseViewModel(course);
    mainViewModel.courseViewModel.set(courseViewModel);

    authentication = mock(Authentication.class);
    doReturn(groups).when(exerciseDataSource).getGroups(course, authentication);
    doReturn(submissionInfo).when(exerciseDataSource).getSubmissionInfo(exercise, authentication);
    doReturn(exerciseGroups)
        .when(exerciseDataSource)
        .getExerciseGroups(same(course), any(Points.class), same(authentication));

    mainViewModel.authentication.set(authentication);

    exercises = Objects.requireNonNull(mainViewModel.exercisesViewModel.get());
    exercises.getGroupViewModels().get(0).getExerciseViewModels().get(0).setSelected(true);

    moduleName = "MyModule";
    modulePath = Paths.get(moduleName);
    moduleFilePath = modulePath.resolve("MyModule.iml").toString();
    module = mock(Module.class);
    doReturn(moduleFilePath).when(module).getModuleFilePath();
    doReturn(moduleName).when(module).getName();

    filePath = modulePath.resolve(fileName);

    fileFinder = mock(FileFinder.class);
    doReturn(filePath).when(fileFinder).tryFindFile(modulePath, fileName);
    doCallRealMethod().when(fileFinder).findFile(any(), any());
    doCallRealMethod().when(fileFinder).findFiles(any(), any());

    moduleSource = mock(SubmitExerciseCommand.ModuleSource.class);
    doReturn(new Module[] { module }).when(moduleSource).getModules();
    doReturn(module).when(moduleSource).getModule(moduleName);
    doReturn(modulePath).when(moduleSource).findModulePath(moduleName);

    dialogs = new TestDialogs();
    messenger = mock(Messenger.class);

    context = mock(SubmitExerciseCommand.Context.class);
    doReturn(dialogs).when(context).getDialogs();
    doReturn(messenger).when(context).getMessenger();
    doReturn(mainViewModel).when(context).getMainViewModel();
    doReturn(moduleSource).when(context).getModuleSource();

    moduleSelectionDialogFactory = spy(new TestDialogs.FactoryImpl<>(viewModel -> {
      viewModel.selectedModule.set(viewModel.getModules()[0]);
      return true;
    }));
    dialogs.register(ModuleSelectionViewModel.class, moduleSelectionDialogFactory);

    submissionDialogFactory = spy(new TestDialogs.FactoryImpl<>(viewModel -> {
      // we select the second group of the list (the first is "submit alone")
      viewModel.selectedGroup.set(viewModel.getAvailableGroups().get(1));
      return true;
    }));
    dialogs.register(SubmissionViewModel.class, submissionDialogFactory);

    command = new SubmitExerciseCommand(fileFinder);
  }

  @Test
  public void testSubmitExerciseAction() throws IOException {
    command.execute(context);

    ArgumentCaptor<Submission> submissionArg = ArgumentCaptor.forClass(Submission.class);
    verify(exerciseDataSource).submit(submissionArg.capture(), same(authentication));

    Submission submission = submissionArg.getValue();
    assertEquals(group, submission.getGroup());
    assertEquals(exercise, submission.getExercise());

    Map<String, Path> files = new HashMap<>();
    files.put(fileKey, filePath);
    assertThat(submission.getFiles(), is(files));

    verifyNoInteractions(messenger);
  }

  @Test
  public void testNotifiesOfMissingFile() throws IOException {
    doReturn(null).when(fileFinder).tryFindFile(modulePath, fileName);

    command.execute(context);

    verifyNoInteractions(submissionDialogFactory);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<MissingFileMessage> messageArg =
        ArgumentCaptor.forClass(MissingFileMessage.class);

    verify(messenger).show(messageArg.capture());

    MissingFileMessage message = messageArg.getValue();
    assertEquals(fileName, message.getFilename());
    assertEquals(modulePath, message.getPath());

    verifyNoMoreInteractions(messenger);
  }

  @Test
  public void testNotifiesOfMissingModule()
      throws IOException, SubmitExerciseCommand.ModuleMissingException {
    String nonexistentModuleName = "nonexistent module";
    SubmitExerciseCommand.ModuleMissingException exception =
        new SubmitExerciseCommand.ModuleMissingException(nonexistentModuleName);
    doThrow(exception).when(moduleSource).findModulePath(nonexistentModuleName);

    Map<Long, Map<String, String>> map = Collections.singletonMap(exerciseId,
        Collections.singletonMap("en", nonexistentModuleName));
    doReturn(map).when(course).getExerciseModules();

    command.execute(context);

    verifyNoInteractions(moduleSelectionDialogFactory);
    verifyNoInteractions(submissionDialogFactory);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<MissingModuleMessage> messageArg =
        ArgumentCaptor.forClass(MissingModuleMessage.class);

    verify(messenger).show(messageArg.capture());

    MissingModuleMessage message = messageArg.getValue();
    assertEquals(nonexistentModuleName, message.getModuleName());

    verifyNoMoreInteractions(messenger);
  }

  @Test
  public void testNotifiesExerciseNotSubmittable() throws IOException {
    doReturn(new SubmissionInfo(1, new SubmittableFile[0]))
        .when(exerciseDataSource)
        .getSubmissionInfo(exercise, authentication);

    command.execute(context);

    verifyNoInteractions(moduleSelectionDialogFactory);
    verifyNoInteractions(submissionDialogFactory);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<NotSubmittableMessage> messageArg =
        ArgumentCaptor.forClass(NotSubmittableMessage.class);

    verify(messenger).show(messageArg.capture());

    NotSubmittableMessage message = messageArg.getValue();
    assertSame(exercise, message.getExercise());

    verifyNoMoreInteractions(messenger);
  }

  @Test
  public void testNotifiesOfNetworkErrorWhenGettingSubmissionHistory() throws IOException {
    IOException exception = new IOException();
    doThrow(exception).when(exerciseDataSource).getSubmissionHistory(exercise, authentication);

    command.execute(context);

    verifyNoInteractions(submissionDialogFactory);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<NetworkErrorMessage> messageArg =
        ArgumentCaptor.forClass(NetworkErrorMessage.class);

    verify(messenger).show(messageArg.capture());

    NetworkErrorMessage message = messageArg.getValue();
    assertSame(exception, message.getException());

    verifyNoMoreInteractions(messenger);
  }

  @Test
  public void testNotifiesOfNetworkErrorWhenGettingGroups() throws IOException {
    IOException exception = new IOException();
    doThrow(exception).when(exerciseDataSource).getGroups(course, authentication);

    command.execute(context);

    verifyNoInteractions(submissionDialogFactory);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<NetworkErrorMessage> messageArg =
        ArgumentCaptor.forClass(NetworkErrorMessage.class);

    verify(messenger).show(messageArg.capture());

    NetworkErrorMessage notification = messageArg.getValue();
    assertSame(exception, notification.getException());

    verifyNoMoreInteractions(messenger);
  }

  @Test
  public void testNotifiesOfNetworkErrorWhenGettingSubmissionInfo() throws IOException {
    IOException exception = new IOException();
    doThrow(exception).when(exerciseDataSource).getSubmissionInfo(exercise, authentication);

    command.execute(context);

    verifyNoInteractions(moduleSelectionDialogFactory);
    verifyNoInteractions(submissionDialogFactory);
    verify(exerciseDataSource, never()).submit(any(), any());

    ArgumentCaptor<NetworkErrorMessage> messageArg =
        ArgumentCaptor.forClass(NetworkErrorMessage.class);

    verify(messenger).show(messageArg.capture());

    NetworkErrorMessage notification = messageArg.getValue();
    assertSame(exception, notification.getException());

    verifyNoMoreInteractions(messenger);
  }

  @Test
  public void testNotifiesOfNetworkErrorWhenSubmitting() throws IOException {
    IOException exception = new IOException();
    doThrow(exception).when(exerciseDataSource).submit(any(), same(authentication));

    command.execute(context);

    ArgumentCaptor<NetworkErrorMessage> messageArg =
        ArgumentCaptor.forClass(NetworkErrorMessage.class);

    verify(messenger).show(messageArg.capture());

    NetworkErrorMessage message = messageArg.getValue();
    assertSame(exception, message.getException());

    verifyNoMoreInteractions(messenger);
  }

  @Test
  public void testModuleSelectionDialogCancel() throws IOException {
    dialogs.register(SubmissionViewModel.class, (submission, none) -> () -> false);

    command.execute(context);

    verify(exerciseDataSource, never()).submit(any(), any());

    verifyNoInteractions(messenger);
  }
}
