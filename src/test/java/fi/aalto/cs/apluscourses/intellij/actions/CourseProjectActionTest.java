package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseSelectionViewModel;
import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogs;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.PostponedRunnable;
import fi.aalto.cs.apluscourses.utils.async.ImmediateTaskManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CourseProjectActionTest extends BasePlatformTestCase {

  private class TestDialogs implements CourseProjectActionDialogs {

    private boolean doCancel;
    private boolean doRestart;
    private boolean doOptOut;

    public TestDialogs(boolean doCancel, boolean doRestart, boolean doOptOut) {
      this.doCancel = doCancel;
      this.doRestart = doRestart;
      this.doOptOut = doOptOut;
    }

    @Override
    public boolean showCourseSelectionDialog(
        @NotNull Project project, @NotNull CourseSelectionViewModel courseSelectionViewModel) {
      courseSelectionViewModel.selectedCourseUrl.set("http://localhost:2358");
      return !doCancel;
    }

    @Override
    public boolean showMainDialog(@NotNull Project project,
                                  @NotNull CourseProjectViewModel courseProjectViewModel) {
      courseProjectViewModel.settingsOptOutProperty.set(doOptOut);
      courseProjectViewModel.languageProperty.set("fi");
      return !doCancel;
    }

    @Override
    public boolean showRestartDialog() {
      return doRestart;
    }
  }

  private AnActionEvent anActionEvent;
  private Course emptyCourse;
  private DummySettingsImporter settingsImporter;
  private ComponentInstaller.Factory installerFactory;
  private AtomicInteger restarterCallCount;
  private PostponedRunnable ideRestarter;
  private TestDialogs dialogs;
  private InstallerDialogs.Factory installerDialogsFactory;
  private Notifier notifier;
  private ExecutorService executor;

  class DummySettingsImporter extends SettingsImporter {

    private int importIdeSettingsCallCount = 0;
    private int importProjectSettingsCallCount = 0;

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
    public String currentlyImportedIdeSettings() {
      return "";
    }

    @Override
    public void importProjectSettings(@NotNull Path basePath,
                                      @NotNull Course course)
        throws IOException {
      ++importProjectSettingsCallCount;
    }
  }

  /**
   * Called before each test method call. Initializes private fields.
   */
  public void createMockObjects() {
    anActionEvent = mock(AnActionEvent.class);
    Project project = getProject();
    when(anActionEvent.getProject()).thenReturn(project);

    URL ideSettingsUrl = null;
    try {
      ideSettingsUrl = new URL("https://localhost:23333");
    } catch (MalformedURLException e) {
      // this will never happen
    }

    emptyCourse = new ModelExtensions.TestCourse(
        "ID",
        //  name
        "EMPTY",
        // url
        "http://localhost:1001",
        Collections.emptyList(),
        //  modules
        Collections.emptyList(),
        //  libraries
        Collections.emptyList(),
        //  exerciseModules
        Collections.emptyMap(),
        //  resourceUrls
        Map.of("ideSettings", ideSettingsUrl),
        //  autoInstallComponentNames
        Collections.emptyList(),
        //  replInitialCommands
        Collections.emptyMap(),
        //  courseVersion
        BuildInfo.INSTANCE.courseVersion
    );

    settingsImporter = new DummySettingsImporter();

    installerFactory = new ComponentInstallerImpl.FactoryImpl<>(new ImmediateTaskManager());

    restarterCallCount = new AtomicInteger(0);

    ideRestarter = new PostponedRunnable(restarterCallCount::incrementAndGet, Runnable::run);

    dialogs = new TestDialogs(false, true, false);

    installerDialogsFactory = proj -> module -> true;

    notifier = mock(Notifier.class);

    executor = Executors.newSingleThreadExecutor();
  }

  @Test
  public void testCreateCourseProject() throws InterruptedException {
    createMockObjects();
    AtomicInteger courseFactoryCallCount = new AtomicInteger(0);
    CourseProjectAction action = new CourseProjectAction(
        (url, proj) -> {
          courseFactoryCallCount.getAndIncrement();
          return emptyCourse;
        },
        false,
        settingsImporter,
        installerFactory,
        ideRestarter,
        dialogs,
        installerDialogsFactory,
        notifier,
        executor);

    action.actionPerformed(anActionEvent);
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assert.assertEquals("CourseFactory#fromUrl should get called", 1,
        courseFactoryCallCount.get());

    Assert.assertEquals("The project settings should get imported", 1,
        settingsImporter.getImportProjectSettingsCallCount());
    Assert.assertEquals("The IDE settings should get imported", 1,
        settingsImporter.getImportIdeSettingsCallCount());
    Assert.assertEquals("The IDE should get restarted", 1, restarterCallCount.get());

    verifyNoInteractions(notifier);
  }

  @Test
  public void testNotifiesUserOfCourseInitializationError() throws InterruptedException {
    createMockObjects();
    IOException exception = new IOException();
    CourseProjectAction action = new CourseProjectAction(
        (url, proj) -> {
          throw exception;
        },
        false,
        settingsImporter,
        installerFactory,
        ideRestarter,
        dialogs,
        installerDialogsFactory,
        notifier,
        executor);

    action.actionPerformed(anActionEvent);
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    ArgumentCaptor<NetworkErrorNotification> notificationCaptor =
        ArgumentCaptor.forClass(NetworkErrorNotification.class);
    verify(notifier).notify(notificationCaptor.capture(), same(getProject()));
    NetworkErrorNotification notification = notificationCaptor.getValue();

    assertNotNull(notification);
    assertSame(exception, notification.getException());

    assertEquals("Project settings are not imported after the error", 0,
        settingsImporter.getImportProjectSettingsCallCount());
    assertEquals("IDE settings are not imported after the error", 0,
        settingsImporter.getImportIdeSettingsCallCount());
  }

  @Test
  public void testNotifiesUserOfSettingsImportError() throws InterruptedException {
    createMockObjects();
    IOException exception = new IOException();
    DummySettingsImporter failingSettingsImporter = new DummySettingsImporter() {
      @Override
      public void importProjectSettings(@NotNull Path basePath,
                                        @NotNull Course course)
          throws IOException {
        super.importProjectSettings(basePath, course);
        throw exception;
      }
    };

    CourseProjectAction action = new CourseProjectAction(
        (url, proj) -> emptyCourse,
        false,
        failingSettingsImporter,
        installerFactory,
        ideRestarter,
        dialogs,
        installerDialogsFactory,
        notifier,
        executor);

    action.actionPerformed(anActionEvent);
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    ArgumentCaptor<NetworkErrorNotification> notificationCaptor =
        ArgumentCaptor.forClass(NetworkErrorNotification.class);
    verify(notifier).notify(notificationCaptor.capture(), same(getProject()));
    NetworkErrorNotification notification = notificationCaptor.getValue();

    assertNotNull(notification);
    assertSame(exception, notification.getException());

    Assert.assertEquals("The action attempted to import project settings", 1,
        failingSettingsImporter.getImportProjectSettingsCallCount());
  }

  @Test
  public void testDoesNotImportIdeSettingsIfOptOut() throws InterruptedException {
    createMockObjects();
    CourseProjectAction action = new CourseProjectAction(
        (url, proj) -> emptyCourse,
        false,
        settingsImporter,
        installerFactory,
        ideRestarter,
        new TestDialogs(false, true, true),
        installerDialogsFactory,
        notifier,
        executor);

    action.actionPerformed(anActionEvent);
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assert.assertEquals("IDE settings are not imported", 0,
        settingsImporter.getImportIdeSettingsCallCount());
    Assert.assertEquals("Project settings are imported", 1,
        settingsImporter.getImportProjectSettingsCallCount());
  }

  @Test
  public void ignoretestLetsUserCancelAction() throws InterruptedException {
    createMockObjects();
    CourseProjectAction action = new CourseProjectAction(
        (url, proj) -> emptyCourse,
        true,
        settingsImporter,
        installerFactory,
        ideRestarter,
        new TestDialogs(true, true, true),
        installerDialogsFactory,
        notifier,
        executor);

    action.actionPerformed(anActionEvent);
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assert.assertEquals("Project settings are not imported", 0,
        settingsImporter.getImportProjectSettingsCallCount());
    Assert.assertEquals("IDE settings are not imported", 0,
        settingsImporter.getImportIdeSettingsCallCount());
    Assert.assertEquals("The IDE is not restarted", 0, restarterCallCount.get());
  }



  @Test
  public void testDoesNotRestartIfSettingsOptOut() throws InterruptedException {
    createMockObjects();
    CourseProjectAction action = new CourseProjectAction(
        (url, proj) -> emptyCourse,
        false,
        settingsImporter,
        installerFactory,
        ideRestarter,
        new TestDialogs(false, false, true),
        installerDialogsFactory,
        notifier,
        executor);

    action.actionPerformed(anActionEvent);
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assert.assertEquals("The IDE is not restarted", 0, restarterCallCount.get());
  }
}
