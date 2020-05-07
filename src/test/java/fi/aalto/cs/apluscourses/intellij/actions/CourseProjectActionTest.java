package fi.aalto.cs.apluscourses.intellij.actions;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CourseProjectActionTest {

  Project project;
  AnActionEvent anActionEvent;
  private MainViewModel mainViewModel;
  private Course emptyCourse;

  class DummySettingsImporter implements SettingsImporter {
    public int importIdeSettingsCallCount = 0;
    public int importProjectSettingsCallCount = 0;

    @Override
    public void importIdeSettings(@NotNull Course course) {
      ++importIdeSettingsCallCount;
    }

    @NotNull
    @Override
    public String lastImportedIdeSettings() {
      return "";
    }

    @Override
    public void importProjectSettings(@NotNull Project project, @NotNull Course course) {
      ++importProjectSettingsCallCount;
    }
  }

  /**
   * Called before each test method call.  Initializes private fields.
   */
  @Before
  public void createMockObjects() {
    project = mock(Project.class);
    anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();
    mainViewModel = new MainViewModel();
    emptyCourse = new Course("EMPTY", Collections.emptyList(), Collections.emptyList(),
        Collections.emptyMap(), Collections.emptyMap(), new ModelExtensions.TestComponentSource());
  }

  @Test
  public void testCreateCourseProject() {
    AtomicInteger callCount = new AtomicInteger(0);
    DummySettingsImporter settingsImporter = new DummySettingsImporter();
    TestDialogs dialogs = new TestDialogs(false);
    CourseProjectAction action = new CourseProjectAction(
        p -> mainViewModel,
        (url, project) -> {
          callCount.getAndIncrement();
          return emptyCourse;
        },
        false,
        settingsImporter,
        () -> fail("IdeRestarter#restart should not get called"),
        dialogs);

    action.actionPerformed(anActionEvent);

    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    Assert.assertNotNull("The course of the provided main view model should get initialized",
        courseViewModel);
    Assert.assertEquals("The course should have the correct name", emptyCourse.getName(),
        courseViewModel.getModel().getName());

    Assert.assertEquals("The project settings should get imported", 1,
        settingsImporter.importProjectSettingsCallCount);
    Assert.assertEquals("The IDE settings should get imported", 1,
        settingsImporter.importIdeSettingsCallCount);

    Assert.assertThat("The user should be prompted to restart the IDE",
        dialogs.getLastOkCancelMessage(), containsString("Restart IntelliJ IDEA now"));
    Assert.assertEquals("No error dialogs should be shown", "",
        dialogs.getLastErrorMessage());
  }
}
