package fi.aalto.cs.apluscourses.intellij.actions;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ImportProjectSettingsActionTest {


  private Project project;
  private MainViewModel mainViewModel;
  private AnActionEvent anActionEvent;

  /**
   * Called before each test method call.  Initializes private fields.
   */
  @Before
  public void createMockObjects() throws MalformedURLException {
    project = mock(Project.class);
    mainViewModel = new MainViewModel();

    Map<String, URL> resourceUrls = new HashMap<>();
    resourceUrls.put("projectSettings", new URL("https://example.com"));
    Course course = new Course("course", Collections.emptyList(), Collections.emptyList(),
        Collections.emptyMap(), resourceUrls);
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();
  }

  @Test
  public void testInformCourseHasNoProjectSettings() {
    Course course = new Course("no-project-settings", Collections.emptyList(),
        Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    TestDialogs dialogs = new TestDialogs(true);
    ImportProjectSettingsAction action = new ImportProjectSettingsAction(
        p -> mainViewModel,
        (proj, url) -> fail("ProjectSettingsImporter#doImport should not get called"),
        dialogs);

    action.actionPerformed(anActionEvent);

    assertThat("The user is informed that the course has no custom project settings",
        dialogs.getLastInformationMessage(),
        containsString("does not provide custom project settings"));

    assertThat("The information dialog contains the course name",
        dialogs.getLastInformationMessage(),
        containsString("no-project-settings"));
  }

  @Test
  public void testInformNoProjectOpen() {
    TestDialogs dialogs = new TestDialogs(true);
    ImportProjectSettingsAction action = new ImportProjectSettingsAction(
        p -> mainViewModel,
        (proj, url) -> fail("ProjectSettingsImporter#doImport should not get called"),
        dialogs);

    AnActionEvent e = mock(AnActionEvent.class);
    action.actionPerformed(e);

    assertThat("The user is informed that a project must be open for project settings to be "
        + "imported", dialogs.getLastInformationMessage(),
        containsString("project must be loaded"));
  }

  @Test
  public void testShowsErrorDialog() {
    TestDialogs dialogs = new TestDialogs(true);
    ImportProjectSettingsAction action = new ImportProjectSettingsAction(
        p -> mainViewModel,
        (proj, url) -> {
          throw new IOException();
        },
        dialogs);

    action.actionPerformed(anActionEvent);

    assertThat("The user should be shown an error message", dialogs.getLastErrorMessage(),
        containsString("check your network connection and try again"));
  }

  @Test
  public void testDoesProjectSettingsImport() {
    TestDialogs dialogs = new TestDialogs(true);
    AtomicInteger importCallCount = new AtomicInteger(0);
    ImportProjectSettingsAction action = new ImportProjectSettingsAction(
        p -> mainViewModel,
        (proj, url) -> importCallCount.getAndIncrement(),
        dialogs);

    action.actionPerformed(anActionEvent);

    Assert.assertEquals("The method doImport should get called", 1, importCallCount.get());
  }

}
