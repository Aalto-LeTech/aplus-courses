package fi.aalto.cs.apluscourses.intellij.activities;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupActivity.Background;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseVersionOutdatedError;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseVersionOutdatedWarning;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseVersionTooNewError;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.ProjectKey;
import fi.aalto.cs.apluscourses.intellij.utils.ProjectViewUtil;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationException;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.Stopwatch;
import fi.aalto.cs.apluscourses.utils.Version;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.slf4j.Logger;

public class InitializationActivity implements Background {

  private static final Logger logger = APlusLogger.logger;

  @NotNull
  private final Notifier notifier;

  @NotNull
  private static final Map<ProjectKey, ObservableProperty<Boolean>> initializedProjects
      = new ConcurrentHashMap<>();

  public InitializationActivity() {
    this(new DefaultNotifier());
  }

  public InitializationActivity(@NotNull Notifier notifier) {
    this.notifier = notifier;
  }

  @Override
  public void runActivity(@NotNull Project project) {
    var stopwatch = new Stopwatch();
    logger.info("Starting initialization");
    logger.info("A+ Courses v{}", BuildInfo.INSTANCE.pluginVersion);
    var courseVersion = BuildInfo.INSTANCE.courseVersion;
    logger.info("Course v{}", courseVersion);
    logger.info("Logs made by Jaakko <3");
    PluginSettings pluginSettings = PluginSettings.getInstance();
    pluginSettings.initializeLocalIdeSettings();

    ProjectViewUtil.ignoreFileInProjectView(MODULE_REPL_INITIAL_COMMANDS_FILE_NAME, project);

    var progressViewModel
        = PluginSettings.getInstance().getMainViewModel(project).progressViewModel;

    URL courseConfigurationFileUrl = getCourseUrlFromProject(project);
    if (courseConfigurationFileUrl == null) {
      isInitialized(project).set(true);
      progressViewModel.stopAll();
      logger.info("No course configuration found");
      return;
    }

    Course course;
    try {
      course = Course.fromUrl(courseConfigurationFileUrl, new IntelliJModelFactory(project));
    } catch (UnexpectedResponseException | MalformedCourseConfigurationException e) {
      logger.error("Error occurred while trying to parse a course configuration file", e);
      notifier.notify(new CourseConfigurationError(e), project);
      isInitialized(project).set(true);
      progressViewModel.stopAll();
      return;
    } catch (IOException e) {
      logger.info("IOException occurred while using the HTTP client", e);
      notifier.notify(new NetworkErrorNotification(e), project);
      isInitialized(project).set(true);
      progressViewModel.stopAll();
      return;
    }
    var progress = progressViewModel.start(3, getText("ui.ProgressBarView.loading"), false);
    progress.increment();

    setCustomResources(project, course);
    progress.increment();

    var versionComparison = courseVersion.compareTo(course.getVersion());

    if (versionComparison == Version.ComparisonStatus.MAJOR_TOO_OLD
        || versionComparison == Version.ComparisonStatus.MAJOR_TOO_NEW) {
      if (versionComparison == Version.ComparisonStatus.MAJOR_TOO_OLD) {
        logger.error("A+ Courses version outdated: installed v{}, required v{}", courseVersion, course.getVersion());
      } else {
        logger.error("A+ Courses version too new: installed v{}, required v{}", courseVersion, course.getVersion());
      }
      notifier.notify(
          versionComparison == Version.ComparisonStatus.MAJOR_TOO_OLD
              ? new CourseVersionOutdatedError() : new CourseVersionTooNewError(), project);
      progress.finish();
      return;
    } else if (versionComparison == Version.ComparisonStatus.MINOR_TOO_OLD) {
      logger.error("A+ Courses version outdated: installed v{}, required v{}", courseVersion, course.getVersion());
      notifier.notify(new CourseVersionOutdatedWarning(), project);
    }

    var courseProject = new CourseProject(course, courseConfigurationFileUrl, project,
        new DefaultNotifier());
    PluginSettings.getInstance().registerCourseProject(courseProject);
    isInitialized(project).set(true);
    progress.finish();
    logger.info("Initialization took {}ms", stopwatch.getTimeMs());
  }

  private static final ProjectManagerListener projectListener = new ProjectManagerListener() {
    @Override
    public void projectClosed(@NotNull Project project) {
      initializedProjects.remove(new ProjectKey(project));
      ProjectManager.getInstance().removeProjectManagerListener(project, this);
    }
  };

  /**
   * Returns the observable boolean corresponding to whether the initialization activity for the
   * given project has completed or not.
   */
  @NotNull
  public static ObservableProperty<Boolean> isInitialized(@NotNull Project project) {
    return initializedProjects.computeIfAbsent(new ProjectKey(project), key -> {
      ProjectManager.getInstance().addProjectManagerListener(project, projectListener);
      return new ObservableReadWriteProperty<>(false);
    });
  }

  @Nullable
  private static URL getCourseUrlFromProject(@NotNull Project project) {
    if (project.isDefault()) {
      return null;
    }

    try {
      boolean isCourseProject = PluginSettings
          .getInstance()
          .getCourseFileManager(project)
          .load();
      if (isCourseProject) {
        return PluginSettings.getInstance().getCourseFileManager(project).getCourseUrl();
      } else {
        return null;
      }
    } catch (IOException | JSONException e) {
      logger.error("Malformed project course tag file", e);
      return null;
    }
  }

  private void setCustomResources(@NotNull Project project, @NotNull Course course) {
    var basePath = project.getBasePath();
    if (basePath != null) {
      try {
        if (new SettingsImporter().importCustomProperties(Paths.get(project.getBasePath()),
            course,
            project)) {
          logger.info("Imported custom properties");
        }
      } catch (IOException e) {
        // No custom resources.
      }
    }
  }
}
