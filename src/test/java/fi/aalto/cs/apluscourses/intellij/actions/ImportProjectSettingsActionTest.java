package fi.aalto.cs.apluscourses.intellij.actions;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.base.Dialogs;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ImportProjectSettingsActionTest {

  private class TestDialogs implements Dialogs {
    private boolean answerOk;

    private String lastInformationMessage = "";
    private String lastErrorMessage = "";
    private String lastOkCancelMessage = "";

    public TestDialogs(boolean answerOk) {
      this.answerOk = answerOk;
    }

    @Override
    public void showInformationDialog(@NotNull String message, @NotNull String title) {
      lastInformationMessage = message;
    }

    @Override
    public void showErrorDialog(@NotNull String message, @NotNull String title) {
      lastErrorMessage = message;
    }

    @Override
    public boolean showOkCancelDialog(@NotNull String message,
                                      @NotNull String title,
                                      @NotNull String okText,
                                      @NotNull String cancelText) {
      lastOkCancelMessage = message;
      return answerOk;
    }

    public String getLastInformationMessage() {
      return lastInformationMessage;
    }

    public String getLastErrorMessage() {
      return lastErrorMessage;
    }

    public String getLastOkCancelMessage() {
      return lastOkCancelMessage;
    }
  }

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
    Course course =
        new Course("course", Collections.emptyList(), Collections.emptyMap(), resourceUrls);
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    anActionEvent = mock(AnActionEvent.class);
    doReturn(project).when(anActionEvent).getProject();
  }

  @Test
  public void testInformCourseHasNoProjectSettings() {
    Course course = new Course("no-project-settings", Collections.emptyList(),
        Collections.emptyMap(), Collections.emptyMap());
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    TestDialogs dialogs = new TestDialogs(true);
    ImportProjectSettingsAction action = new ImportProjectSettingsAction(
        p -> mainViewModel,
        (project, url) -> fail("ProjectSettingsImporter#doImport should not get called"),
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
  public void testShowsErrorDialog() {
    TestDialogs dialogs = new TestDialogs(true);
    ImportProjectSettingsAction action = new ImportProjectSettingsAction(
        p -> mainViewModel,
        (project, url) -> {
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
        (project, url) -> importCallCount.getAndIncrement(),
        dialogs);

    action.actionPerformed(anActionEvent);

    Assert.assertEquals("The method doImport should get called", 1, importCallCount.get());
  }

}
