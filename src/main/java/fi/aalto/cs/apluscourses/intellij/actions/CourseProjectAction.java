package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporterImpl;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.CourseFileManager;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationFileException;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogs;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogsImpl;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseProjectAction extends AnAction {

  private static final Logger logger = LoggerFactory.getLogger(CourseProjectAction.class);

  @NotNull
  private CourseFactory courseFactory;

  private boolean createCourseFile;

  @NotNull
  private SettingsImporter settingsImporter;

  @NotNull
  private IdeRestarter ideRestarter;

  @NotNull
  private CourseProjectActionDialogs dialogs;

  /**
   * Construct a course project action with the given main view model provider and dialogs.
   */
  public CourseProjectAction(@NotNull CourseFactory courseFactory,
                             boolean createCourseFile,
                             @NotNull SettingsImporter settingsImporter,
                             @NotNull IdeRestarter ideRestarter,
                             @NotNull CourseProjectActionDialogs dialogs) {
    this.courseFactory = courseFactory;
    this.createCourseFile = createCourseFile;
    this.settingsImporter = settingsImporter;
    this.ideRestarter = ideRestarter;
    this.dialogs = dialogs;
  }

  /**
   * Construct a course project action with sensible defaults.
   */
  public CourseProjectAction() {
    this(
        (url, project) -> Course.fromUrl(url, new IntelliJModelFactory(project)),
        true,
        new SettingsImporterImpl(),
        () -> ((ApplicationEx) ApplicationManager.getApplication()).restart(true),
        new CourseProjectActionDialogsImpl());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();

    URL selectedCourseUrl = getSelectedCourseUrl();
    if (selectedCourseUrl == null) {
      return;
    }

    Course course = tryGetCourse(project, selectedCourseUrl);
    if (course == null) {
      return;
    }

    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(course, settingsImporter.currentlyImportedIdeSettings());
    if (!dialogs.showMainDialog(project, courseProjectViewModel)) {
      return;
    }

    startAutoInstalls(project, course, courseProjectViewModel.userWantsRestart());

    if (!tryCreateCourseFile(project, selectedCourseUrl)) {
      return;
    }

    if (!tryImportProjectSettings(project, course)) {
      return;
    }

    if (!courseProjectViewModel.userOptsOutOfSettings()) {
      tryImportIdeSettings(course);
    }

    if (createCourseFile) {
      // The course file not created in testing.
      PluginSettings.getInstance().createUpdatingMainViewModel(project);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    // This action is available only if a non-default project is open
    Project project = e.getProject();
    e.getPresentation().setEnabledAndVisible(project != null && !project.isDefault());
  }

  @FunctionalInterface
  public interface CourseFactory {
    @NotNull
    Course fromUrl(@NotNull URL courseUrl, @NotNull Project project)
        throws IOException, MalformedCourseConfigurationFileException;
  }

  @FunctionalInterface
  public interface IdeRestarter {
    void restart();
  }

  @Nullable
  private URL getSelectedCourseUrl() {
    // TODO: show a dialog with a list of courses and a URL field for custom courses, from which
    // the user selects a course.
    try {
      return new URL(PluginSettings.COURSE_CONFIGURATION_FILE_URL);
    } catch (MalformedURLException e) {
      // User entered an invalid URL (or the default list has an invalid URL, which would be a bug)
      logger.error("Malformed course configuration file URL", e);
      return null;
    }
  }

  /**
   * Returns a course created from the course configuration file at the given URL. The user is
   * notified if the course initialization fails.
   * @param project   The currently open project.
   * @param courseUrl The URL from which the course configuration file is downloaded.
   * @return The course created from the course configuration file or null in case of an error.
   */
  @Nullable
  private Course tryGetCourse(@NotNull Project project, @NotNull URL courseUrl) {
    try {
      return courseFactory.fromUrl(courseUrl, project);
    } catch (IOException e) {
      notifyNetworkError();
      return null;
    } catch (MalformedCourseConfigurationFileException e) {
      logger.error("Malformed course configuration file", e);
      notifyMalformedCourseConfiguration();
      return null;
    }
  }

  private void startAutoInstalls(@NotNull Project project,
                                 @NotNull Course course,
                                 boolean restartWhenFinished) {
    ComponentInstaller.Factory factory
        = new ComponentInstallerImpl.FactoryImpl<>(new SimpleAsyncTaskManager());
    ComponentInstaller installer = factory.getInstallerFor(course);

    Runnable callback = null;
    if (restartWhenFinished) {
      callback = () -> ApplicationManager.getApplication().invokeLater(() -> {
        if (dialogs.showRestartDialog(project)) {
          ideRestarter.restart();
        }
      });
    }

    installer.installAsync(course.getAutoInstallComponents(), callback);
  }

  /**
   * Creates a file in the project settings directory which contains the given course configuration
   * file URL.
   * @return True if the file was successfully created, false otherwise.
   */
  private boolean tryCreateCourseFile(@NotNull Project project, @NotNull URL courseUrl) {
    try {
      if (createCourseFile) {
        CourseFileManager.getInstance().createAndLoad(project, courseUrl);
      }
      return true;
    } catch (IOException e) {
      logger.error("Failed to create course file", e);
      return false;
    }
  }

  /**
   * Tries importing project settings from the given course. Shows an error dialog to the user if a
   * network error occurs.
   * @return True if project settings were successfully imported, false otherwise.
   */
  private boolean tryImportProjectSettings(@NotNull Project project, @NotNull Course course) {
    try {
      settingsImporter.importProjectSettings(project, course);
      return true;
    } catch (IOException e) {
      notifyNetworkError();
      return false;
    }
  }

  /**
   * Tries importing IDE settings from the given course. Shows an error dialog to the user if a
   * network error occurs.
   * @return True if IDE settings were successfully imported, false otherwise.
   */
  private boolean tryImportIdeSettings(@NotNull Course course) {
    try {
      settingsImporter.importIdeSettings(course);
      return true;
    } catch (IOException e) {
      notifyNetworkError();
      return false;
    }
  }

  private void notifyNetworkError() {
    dialogs.showErrorDialog("An error occurred while creating a course project. Please check your "
        + "network connection and try again, or contact the course staff if the issue persists.",
        "Network Error");
  }

  private void notifyMalformedCourseConfiguration() {
    dialogs.showErrorDialog("An error occurred while reading the course configuration. Please "
        + "contact the course staff.", "Course Configuration Error");
  }

}
