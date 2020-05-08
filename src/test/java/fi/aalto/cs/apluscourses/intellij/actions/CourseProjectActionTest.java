package fi.aalto.cs.apluscourses.intellij.actions;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CourseProjectActionTest {

  private AnActionEvent anActionEvent;
  private MainViewModel mainViewModel;
  private Course emptyCourse;
  private DummySettingsImporter settingsImporter;
  private DummyIdeRestarter ideRestarter;
  private TestDialogs dialogs;

  class DummySettingsImporter implements SettingsImporter {
    private int importIdeSettingsCallCount = 0;
    private int importProjectSettingsCallCount = 0;
    private String lastIdeSettingsCourseName = "";

    public int getImportIdeSettingsCallCount() {
      return importIdeSettingsCallCount;
    }

    public int getImportProjectSettingsCallCount() {
      return importProjectSettingsCallCount;
    }

    @Override
    public void importIdeSettings(@NotNull Course course) {
      ++importIdeSettingsCallCount;
    }

    @NotNull
    @Override
    public String lastImportedIdeSettings() {
      return lastIdeSettingsCourseName;
    }

    public void setLastImportedIdeSettings(@NotNull String courseName) {
      lastIdeSettingsCourseName = courseName;
    }

    @Override
    public void importProjectSettings(@NotNull Project project, @NotNull Course course) {
      ++importProjectSettingsCallCount;
    }
  }

  class DummyIdeRestarter implements CourseProjectAction.IdeRestarter {
    private int callCount = 0;

    @Override
    public void restart() {
      ++callCount;
    }

    int getCallCount() {
      return callCount;
    }
  }

  /**
   * Called before each test method call.  Initializes private fields.
   */
  @Before
  public void createMockObjects() {
    Project project = mock(Project.class);
    anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();
    mainViewModel = new MainViewModel();
    emptyCourse = new Course("EMPTY", Collections.emptyList(), Collections.emptyList(),
        Collections.emptyMap(), Collections.emptyMap(), new ModelExtensions.TestComponentSource());

    settingsImporter = new DummySettingsImporter();

    ideRestarter = new DummyIdeRestarter();

    dialogs = new TestDialogs(true);
  }

  @Test
  public void testCreateCourseProject() {
    AtomicInteger courseFactoryCallCount = new AtomicInteger(0);
    CourseProjectAction action = new CourseProjectAction(
        p -> mainViewModel,
        (url, proj) -> {
          courseFactoryCallCount.getAndIncrement();
          return emptyCourse;
        },
        false,
        settingsImporter,
        ideRestarter,
        dialogs);

    action.actionPerformed(anActionEvent);

    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    Assert.assertEquals("CourseFactory#fromUrl should get called", 1,
        courseFactoryCallCount.get());
    Assert.assertNotNull("The course of the provided main view model should get initialized",
        courseViewModel);
    Assert.assertEquals("The course should have the correct name", emptyCourse.getName(),
        courseViewModel.getModel().getName());

    Assert.assertEquals("The project settings should get imported", 1,
        settingsImporter.getImportProjectSettingsCallCount());
    Assert.assertEquals("The IDE settings should get imported", 1,
        settingsImporter.getImportIdeSettingsCallCount());

    Assert.assertThat("The user should be prompted to restart the IDE",
        dialogs.getLastOkCancelMessage(), containsString("Restart IntelliJ IDEA now"));
    Assert.assertEquals("IdeRestarter#restart should get called", 1, ideRestarter.getCallCount());
    Assert.assertEquals("No error dialogs should be shown", "",
        dialogs.getLastErrorMessage());
  }

  @Test
  public void testDoesNotImportIdeSettingsAgain() {
    settingsImporter.setLastImportedIdeSettings(emptyCourse.getName());
    CourseProjectAction action = new CourseProjectAction(
        p -> mainViewModel,
        (url, proj) -> emptyCourse,
        false,
        settingsImporter,
        () -> { },
        dialogs);

    action.actionPerformed(anActionEvent);

    Assert.assertEquals("IDE settings shouldn't get imported", 0,
        settingsImporter.getImportIdeSettingsCallCount());
    Assert.assertEquals("The user should not be prompted to restart the IDE", "",
        dialogs.getLastOkCancelMessage());
    Assert.assertEquals("The IDE should not get restarted", 0, ideRestarter.getCallCount());
    Assert.assertEquals("Project settings should still get imported", 1,
        settingsImporter.getImportProjectSettingsCallCount());
  }

  @Test
  public void testNotifiesUserOfCourseInitializationError() {
    CourseProjectAction action = new CourseProjectAction(
        p -> mainViewModel,
        (url, proj) -> {
          throw new IOException();
        },
        false,
        settingsImporter,
        ideRestarter,
        dialogs);

    action.actionPerformed(anActionEvent);

    Assert.assertThat("The user is notified of the course initialization error",
        dialogs.getLastErrorMessage(), containsString("Please check your network connection"));
    Assert.assertEquals("Project settings are not imported after the error", 0,
        settingsImporter.getImportProjectSettingsCallCount());
    Assert.assertEquals("IDE settings are not imported after the error", 0,
        settingsImporter.getImportIdeSettingsCallCount());
    Assert.assertNull("The course of the provided main view model isn't initialized",
        mainViewModel.courseViewModel.get());
  }
}
