package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.CourseFileManager;
import fi.aalto.cs.apluscourses.intellij.utils.ExtendedDataContext;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationFileException;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogs;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogsImpl;
import fi.aalto.cs.apluscourses.utils.PostponedRunnable;
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
  private final CourseFactory courseFactory;

  private final boolean createCourseFile;

  @NotNull
  private final SettingsImporter settingsImporter;

  @NotNull
  private final ComponentInstaller.Factory installerFactory;

  @NotNull
  private final PostponedRunnable ideRestarter;

  @NotNull
  private final CourseProjectActionDialogs dialogs;

  @NotNull
  private final InstallerDialogs.Factory installerDialogsFactory;

  /**
   * Construct a course project action with the given parameters.
   *
   * @param courseFactory    An instance of {@link CourseFactory} that is used to create a course
   *                         instance from a URL.
   * @param createCourseFile Determines whether a course file is created or not. This is useful
   *                         mostly for testing purposes.
   * @param settingsImporter An instance of {@link SettingsImporter} that is used to import IDE and
   *                         project settings.
   * @param installerFactory The factory used to create the component installer. The component
   *                         installer is then used to install the automatically installed
   *                         components of the course. This is useful mainly for testing.
   * @param ideRestarter     A {@link PostponedRunnable} that is used to restart the IDE after
   *                         everything related to the course project action is done. In practice,
   *                         this is either immediately after the action is done, or after all
   *                         automatically installed components for the course have been installed.
   *                         Since the installation of automatically installed components may take
   *                         quite a while, it is advisable for this to show the user a confirmation
   *                         dialog regarding the restart.
   */
  public CourseProjectAction(@NotNull CourseFactory courseFactory,
                             boolean createCourseFile,
                             @NotNull SettingsImporter settingsImporter,
                             @NotNull ComponentInstaller.Factory installerFactory,
                             @NotNull PostponedRunnable ideRestarter,
                             @NotNull CourseProjectActionDialogs dialogs,
                             @NotNull InstallerDialogs.Factory installerDialogsFactory) {
    this.courseFactory = courseFactory;
    this.createCourseFile = createCourseFile;
    this.settingsImporter = settingsImporter;
    this.installerFactory = installerFactory;
    this.ideRestarter = ideRestarter;
    this.dialogs = dialogs;
    this.installerDialogsFactory = installerDialogsFactory;
  }

  /**
   * Construct a course project action with sensible defaults.
   */
  public CourseProjectAction() {
    this.courseFactory = (url, project) -> Course.fromUrl(url, new IntelliJModelFactory(project));
    this.createCourseFile = true;
    this.settingsImporter = new SettingsImporter();
    this.installerFactory = new ComponentInstallerImpl.FactoryImpl<>(new SimpleAsyncTaskManager());
    this.dialogs = new CourseProjectActionDialogsImpl();
    this.ideRestarter = new PostponedRunnable(() -> {
      if (dialogs.showRestartDialog()) {
        ((ApplicationEx) ApplicationManager.getApplication()).restart(true);
      }
    });
    this.installerDialogsFactory = InstallerDialogs::new;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();

    if (project == null) {
      return;
    }

    URL selectedCourseUrl = getSelectedCourseUrl();
    if (selectedCourseUrl == null) {
      return;
    }

    if (!tryCreateCourseFile(project, selectedCourseUrl)) {
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

    startAutoInstallsWithRestart(course, courseProjectViewModel.userWantsRestart(), project);

    if (!tryImportProjectSettings(project, course)) {
      return;
    }

    ActionUtil.launch(GetSubmissionsDashboardAction.ACTION_ID,
        new ExtendedDataContext().withProject(project));

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

  private void startAutoInstallsWithRestart(@NotNull Course course,
                                            boolean restartWhenFinished,
                                            @NotNull Project project) {
    ComponentInstaller.Dialogs installerDialogs = installerDialogsFactory.getDialogs(project);
    ComponentInstaller installer = installerFactory.getInstallerFor(course, installerDialogs);
    if (!restartWhenFinished) {
      installer.installAsync(course.getAutoInstallComponents());
    } else {
      installer.installAsync(course.getAutoInstallComponents(), ideRestarter);
    }
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
