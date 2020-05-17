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

public class ImportIdeSettingsActionTest {

  private Project project;
  private MainViewModel mainViewModel;
  private AnActionEvent anActionEvent;

  private static ImportIdeSettingsAction.IdeSettingsImporter failOnCallImporter
      = url -> fail("IdeSettingsImporter#doImport should not get called");
  private static ImportIdeSettingsAction.IdeRestarter failOnCallRestarter
      = () -> fail("IdeRestarter#restart should not get called");

  /**
   * Called before each test method call.  Initializes private fields.
   */
  @Before
  public void createMockObjects() throws MalformedURLException {
    project = mock(Project.class);
    mainViewModel = new MainViewModel();

    Map<String, URL> resourceUrls = new HashMap<>();
    resourceUrls.put("ideSettings", new URL("https://example.com"));
    Course course = new Course("course", Collections.emptyList(), Collections.emptyList(),
        Collections.emptyMap(), resourceUrls);
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();
  }

  @Test
  public void testInformsCourseHasNoIdeSettings() {
    Course course = new Course("no-ide-settings", Collections.emptyList(),
        Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    TestDialogs dialogs = new TestDialogs(false);
    ImportIdeSettingsAction action = new ImportIdeSettingsAction(
        p -> mainViewModel,
        failOnCallImporter,
        dialogs,
        failOnCallRestarter);

    action.actionPerformed(anActionEvent);

    assertThat("The user should be informed that the course has no custom IDE settings",
        dialogs.getLastInformationMessage(),
        containsString("does not provide custom IDE settings"));
    assertThat("The information dialog contains the course name",
        dialogs.getLastInformationMessage(),
        containsString("no-ide-settings"));
  }

  @Test
  public void testLetsUserCancelImport() {
    TestDialogs dialogs = new TestDialogs(false);
    ImportIdeSettingsAction action = new ImportIdeSettingsAction(
        p -> mainViewModel,
        failOnCallImporter,
        dialogs,
        failOnCallRestarter);

    action.actionPerformed(anActionEvent);

    assertThat("The user should be shown a dialog with the option to cancel the import",
        dialogs.getLastOkCancelMessage(), containsString("settings are overwritten"));
    Assert.assertEquals("No other dialogs should be shown", "",
        dialogs.getLastInformationMessage());
    Assert.assertEquals("No other dialogs should be shown", "", dialogs.getLastErrorMessage());
  }

  @Test
  public void testShowsErrorDialog() {
    TestDialogs dialogs = new TestDialogs(true);
    ImportIdeSettingsAction action = new ImportIdeSettingsAction(
        p -> mainViewModel,
        url -> {
          throw new IOException();
        },
        dialogs,
        failOnCallRestarter);

    action.actionPerformed(anActionEvent);

    assertThat("The user should be shown an error message", dialogs.getLastErrorMessage(),
        containsString("check your network connection and try again"));
    Assert.assertEquals("No information dialog is shown", "", dialogs.getLastInformationMessage());
  }

  @Test
  public void testDoesSettingsImport() {
    TestDialogs dialogs = new TestDialogs(true);
    AtomicInteger importCallCount = new AtomicInteger(0);
    ImportIdeSettingsAction action = new ImportIdeSettingsAction(
        p -> mainViewModel,
        url -> importCallCount.getAndIncrement(),
        dialogs,
        () -> { });

    action.actionPerformed(anActionEvent);

    Assert.assertEquals("The method doImport should get called", 1, importCallCount.get());
  }

  @Test
  public void testProposesAndDoesRestart() {
    TestDialogs dialogs = new TestDialogs(true);
    AtomicInteger restartCallCount = new AtomicInteger(0);
    ImportIdeSettingsAction action = new ImportIdeSettingsAction(
        p -> mainViewModel,
        url -> { },
        dialogs,
        restartCallCount::getAndIncrement);

    action.actionPerformed(anActionEvent);

    assertThat("Restarting the IDE should be suggested to the user",
        dialogs.getLastOkCancelMessage(), containsString("Restart the IDE now"));
    Assert.assertEquals("The restart method should get called", 1, restartCallCount.get());
  }
}
