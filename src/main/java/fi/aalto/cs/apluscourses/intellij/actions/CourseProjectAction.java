package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.APlusProject;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporterImpl;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationFileException;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogs;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogsImpl;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseProjectAction extends AnAction {

  private static final Logger logger = LoggerFactory.getLogger(CourseProjectAction.class);

  @NotNull
  private MainViewModelProvider mainViewModelProvider;

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
  public CourseProjectAction(@NotNull MainViewModelProvider mainViewModelProvider,
                             @NotNull CourseFactory courseFactory,
                             boolean createCourseFile,
                             @NotNull SettingsImporter settingsImporter,
                             @NotNull IdeRestarter ideRestarter,
                             @NotNull CourseProjectActionDialogs dialogs) {
    this.mainViewModelProvider = mainViewModelProvider;
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
        PluginSettings.getInstance(),
        (url, project) -> {
          InputStream inputStream = CoursesClient.fetchJson(url);
          return Course.fromConfigurationData(new InputStreamReader(inputStream),
              url.toString(), new IntelliJModelFactory(project));
        },
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

    Course course = tryInitializeCourse(project, selectedCourseUrl);
    if (course == null) {
      return;
    }

    if (!tryCreateCourseFile(project, selectedCourseUrl)) {
      return;
    }

    if (!tryImportProjectSettings(project, course)) {
      return;
    }

    // Importing IDE settings potentially restarts the IDE, so it's the last action. If the
    // IDE settings for the course have already been imported, do nothing.
    if (!course.getName().equals(settingsImporter.lastImportedIdeSettings())) {
      boolean shouldImport = dialogs.showImportIdeSettingsDialog(project);
      if (shouldImport && tryImportIdeSettings(course) && userWantsToRestart()) {
        ideRestarter.restart();
      }
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
   * Parses the course configuration file from the given URL and updates the course view model of
   * the main view model provider. The user is notified if the initialization fails.
   * @param project   The currently open project.
   * @param courseUrl The URL from which the course configuration file is downloaded.
   * @return The course created from the course configuration file or null in case of an error.
   */
  @Nullable
  private Course tryInitializeCourse(@NotNull Project project, @NotNull URL courseUrl) {
    try {
      Course course = courseFactory.fromUrl(courseUrl, project);
      mainViewModelProvider
          .getMainViewModel(project)
          .courseViewModel
          .set(new CourseViewModel(course));
      return course;
    } catch (IOException e) {
      notifyNetworkError();
      return null;
    } catch (MalformedCourseConfigurationFileException e) {
      logger.error("Malformed course configuration file", e);
      notifyMalformedCourseConfiguration();
      return null;
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
        File courseFile = new APlusProject(project).getCourseFilePath().toFile();
        FileUtils.writeStringToFile(courseFile, courseUrl.toString(), StandardCharsets.UTF_8);
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

  private boolean userWantsToRestart() {
    return dialogs.showOkCancelDialog(
        "Settings were imported successfully. Restart IntelliJ IDEA now to reload the settings?",
        "Restart Needed", "Yes", "No");
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
