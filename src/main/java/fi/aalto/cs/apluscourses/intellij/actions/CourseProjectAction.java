package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseConfigListErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseFileError;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseVersionOutdatedError;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseVersionTooNewError;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationException;
import fi.aalto.cs.apluscourses.presentation.CourseItemViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseSelectionViewModel;
import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogs;
import fi.aalto.cs.apluscourses.ui.courseproject.CourseProjectActionDialogsImpl;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import fi.aalto.cs.apluscourses.ui.utils.PluginInstallerCallback;
import fi.aalto.cs.apluscourses.ui.utils.PluginInstallerDialogs;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.PluginAutoInstaller;
import fi.aalto.cs.apluscourses.utils.PostponedRunnable;
import fi.aalto.cs.apluscourses.utils.Version;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class CourseProjectAction extends AnAction {

  private static final Logger logger = APlusLogger.logger;

  public static final String ACTION_ID = CourseProjectAction.class.getCanonicalName();

  @NotNull
  private final CourseFactory courseFactory;

  private final boolean useCourseFile;

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

  @NotNull
  private final Notifier notifier;

  private final ExecutorService executor;

  private static final String COURSE_LIST_URL =
      "https://version.aalto.fi/gitlab/aplus-courses/course-config-urls/-/raw/main/courses.yaml";

  private static final List<CourseItemViewModel> FALLBACK_COURSES = List.of(
      new CourseItemViewModel("Ohjelmointistudio 2 / Programming Studio A", "Spring 2023",
          "https://gitmanager.cs.aalto.fi/static/studios-spring-2023/modules/s2_course_config.json"),
      new CourseItemViewModel("O1", "Fall 2022",
          "https://gitmanager.cs.aalto.fi/static/O1_2022/modules/o1_course_config.json")
  );

  /**
   * Construct a course project action with the given parameters.
   *
   * @param courseFactory    An instance of {@link CourseFactory} that is used to create a course
   *                         instance from a URL.
   * @param useCourseFile    Determines whether a course file is read/created or not. This is useful
   *                         only for testing purposes.
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
   * @param notifier         Notification bus.
   */
  public CourseProjectAction(@NotNull CourseFactory courseFactory,
                             boolean useCourseFile,
                             @NotNull SettingsImporter settingsImporter,
                             @NotNull ComponentInstaller.Factory installerFactory,
                             @NotNull PostponedRunnable ideRestarter,
                             @NotNull CourseProjectActionDialogs dialogs,
                             @NotNull InstallerDialogs.Factory installerDialogsFactory,
                             @NotNull Notifier notifier,
                             @NotNull ExecutorService executor) {
    this.courseFactory = courseFactory;
    this.useCourseFile = useCourseFile;
    this.settingsImporter = settingsImporter;
    this.installerFactory = installerFactory;
    this.ideRestarter = ideRestarter;
    this.dialogs = dialogs;
    this.installerDialogsFactory = installerDialogsFactory;
    this.notifier = notifier;
    this.executor = executor;
  }

  /**
   * Construct a course project action with sensible defaults.
   */
  public CourseProjectAction() {
    this.courseFactory = (url, project) -> Course.fromUrl(url, new IntelliJModelFactory(project));
    this.useCourseFile = true;
    this.settingsImporter = new SettingsImporter();
    this.installerFactory = new ComponentInstallerImpl.FactoryImpl<>(new SimpleAsyncTaskManager());
    this.dialogs = new CourseProjectActionDialogsImpl();
    this.ideRestarter = new PostponedRunnable(() -> {
      if (dialogs.showRestartDialog()) {
        ((ApplicationEx) ApplicationManager.getApplication()).restart(true);
      }
    });
    this.installerDialogsFactory = InstallerDialogs::new;
    this.notifier = new DefaultNotifier();
    this.executor = JobScheduler.getScheduler();
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    logger.debug("Starting CourseProjectAction");
    Project project = e.getProject();

    if (project == null) {
      return;
    }

    URL courseUrl = tryGetCourseUrl(project);
    if (courseUrl == null) {
      return;
    }

    Course course = tryGetCourse(project, courseUrl);
    if (course == null) {
      return;
    }

    final Boolean pluginDependencyResult = PluginAutoInstaller.ensureDependenciesInstalled(project, notifier,
        course.getRequiredPlugins(), PluginInstallerDialogs::askForInstallationConsentOnCreation);

    if (pluginDependencyResult == null) {
      // the user cancelled dependency installation
      return;
    } else if (!pluginDependencyResult) {
      // new plugins installed, we must restart
      if (!PluginInstallerDialogs.askForIDERestart()) {
        return;
      }

      ((ApplicationEx) ApplicationManager.getApplication()).restart(true);
      return;
    }

    var version = BuildInfo.INSTANCE.courseVersion;
    var versionComparison = version.compareTo(course.getVersion());

    if (versionComparison == Version.ComparisonStatus.MAJOR_TOO_OLD
        || versionComparison == Version.ComparisonStatus.MAJOR_TOO_NEW) {
      if (versionComparison == Version.ComparisonStatus.MAJOR_TOO_OLD) {
        logger.warn("A+ Courses version outdated: installed {}, required {}", version, course.getVersion());
      } else {
        logger.warn("A+ Courses version too new: installed {}, required {}", version, course.getVersion());
      }
      notifier.notify(
          versionComparison == Version.ComparisonStatus.MAJOR_TOO_OLD
              ? new CourseVersionOutdatedError() : new CourseVersionTooNewError(), project);
      return;
    }

    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(course, settingsImporter.currentlyImportedIdeSettings());
    if (!dialogs.showMainDialog(project, courseProjectViewModel)) {
      return;
    }

    String language = Objects.requireNonNull(courseProjectViewModel.languageProperty.get());
    logger.info("Language chosen: {}", language);
    if (!tryCreateCourseFile(project, courseUrl, language)) {
      return;
    }

    boolean importIdeSettings = courseProjectViewModel.shouldApplyNewIdeSettings();
    logger.info("Should apply new IDE settings: {}", importIdeSettings);

    var basePath = Optional.ofNullable(project.getBasePath()).map(Paths::get).orElse(null);
    if (basePath == null) {
      logger.warn("Settings could not be imported because (default?) project does not have path.");
      return;
    }

    if (useCourseFile) {
      // The course file not created in testing.
      var currentProject = PluginSettings.getInstance().getCourseProject(project);
      if (currentProject != null) {
        currentProject.getCourseUpdater().restart();
        currentProject.getExercisesUpdater().restart();
      } else {
        var courseProject = new CourseProject(course, courseUrl, project, notifier);
        PluginSettings.getInstance().registerCourseProject(courseProject);
      }
    }

    Future<?> autoInstallDone = executor.submit(() -> startAutoInstalls(course, project));

    Future<Boolean> projectSettingsImported =
        executor.submit(() -> tryImportProjectSettings(project, basePath, course));

    Future<Boolean> ideSettingsImported =
        executor.submit(() -> importIdeSettings && tryImportIdeSettings(project, course));

    Future<Boolean> customPropertiesImported =
        executor.submit(() -> tryImportCustomProperties(project, basePath, course));

    executor.submit(() -> tryImportFeedbackCss(project, course));

    ComponentDatabase.showToolWindow(ComponentDatabase.APLUS_TOOL_WINDOW, project);

    executor.execute(() -> {
      try {
        autoInstallDone.get();
        if (projectSettingsImported.get() && customPropertiesImported.get() //  NOSONAR
            && importIdeSettings && ideSettingsImported.get()) { //  NOSONAR
          ideRestarter.run();
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      } catch (ExecutionException ex) {
        logger.warn("An exception was thrown in an asynchronous call", ex);
      }
    });
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
        throws IOException, MalformedCourseConfigurationException;
  }

  @Nullable
  private URL tryGetCourseUrl(@NotNull Project project) {
    logger.info("Getting course url");
    try {
      if (useCourseFile && PluginSettings.getInstance().getCourseFileManager(project).load()) {
        logger.info("Course file exists");
        return PluginSettings.getInstance().getCourseFileManager(project).getCourseUrl();
      }

      CourseSelectionViewModel viewModel = new CourseSelectionViewModel();
      Executors.newSingleThreadExecutor().submit(() -> viewModel.courses.set(fetchCourses(project)));
      boolean cancelled = !dialogs.showCourseSelectionDialog(project, viewModel);
      if (cancelled) {
        logger.info("Canceled course selection");
        return null;
      } else {
        var url = new URL(Objects.requireNonNull(viewModel.selectedCourseUrl.get()));
        logger.info("Got url: {}", url);
        return url;
      }
    } catch (MalformedURLException e) {
      // User entered an invalid URL (or the default list has an invalid URL, which would be a bug)
      logger.warn("Malformed course configuration file URL", e);
      notifier.notify(new NetworkErrorNotification(e), project);
      return null;
    } catch (IOException e) {
      notifier.notify(new NetworkErrorNotification(e), project);
      return null;
    }
  }

  private CourseItemViewModel[] fetchCourses(@NotNull Project project) {
    try {
      var url = new URL(COURSE_LIST_URL);
      var courseStream = CoursesClient.fetch(url);
      var yaml = new Yaml();

      @SuppressWarnings("unchecked") var courseList = (List<Map<String, String>>) yaml.load(courseStream);
      return courseList.stream().map(CourseItemViewModel::fromMap).toArray(CourseItemViewModel[]::new);
    } catch (MalformedURLException e) {
      logger.info("Malformed course config list url", e);
    } catch (IOException e) {
      logger.info("Failed to fetch course config list", e);
    } catch (ClassCastException e) {
      logger.info("Course config list yaml corrupted", e);
    }
    notifier.notify(new CourseConfigListErrorNotification(), project);
    return FALLBACK_COURSES.toArray(CourseItemViewModel[]::new);
  }

  /**
   * Returns a course created from the course configuration file at the given URL. The user is
   * notified if the course initialization fails.
   *
   * @param project   The currently open project.
   * @param courseUrl The URL from which the course configuration file is downloaded.
   * @return The course created from the course configuration file or null in case of an error.
   */
  @Nullable
  private Course tryGetCourse(@NotNull Project project, @NotNull URL courseUrl) {
    try {
      logger.debug("Getting course");
      return courseFactory.fromUrl(courseUrl, project);
    } catch (IOException e) {
      logger.warn("Network error", e);
      notifier.notify(new NetworkErrorNotification(e), project);
      return null;
    } catch (MalformedCourseConfigurationException e) {
      logger.warn("Malformed course configuration file", e);
      notifier.notify(new CourseConfigurationError(e), project);
      return null;
    }
  }

  private void startAutoInstalls(@NotNull Course course, @NotNull Project project) {
    ComponentInstaller.Dialogs installerDialogs = installerDialogsFactory.getDialogs(project);
    ComponentInstaller installer = installerFactory.getInstallerFor(course, installerDialogs);
    installer.install(course.getAutoInstallComponents());
  }

  /**
   * Creates a file in the project settings directory which contains the given course configuration
   * file URL and language.
   *
   * @return True if the file was successfully created, false otherwise.
   */
  private boolean tryCreateCourseFile(@NotNull Project project,
                                      @NotNull URL courseUrl,
                                      @NotNull String language) {
    try {
      logger.debug("Creating course file");
      if (useCourseFile) {
        PluginSettings
            .getInstance()
            .getCourseFileManager(project)
            .createAndLoad(courseUrl, language);
      }
      return true;
    } catch (IOException e) {
      logger.warn("Failed to create course file", e);
      notifier.notify(new CourseFileError(e), project);
      return false;
    }
  }

  /**
   * Tries importing project settings from the given course. Shows a notification to the user if a
   * network error occurs.
   *
   * @return True if project settings were successfully imported, false otherwise.
   */
  private boolean tryImportProjectSettings(@NotNull Project project,
                                           @NotNull Path basePath,
                                           @NotNull Course course) {
    try {
      settingsImporter.importProjectSettings(project, basePath, course);
      return true;
    } catch (IOException e) {
      logger.warn("Failed to import project settings", e);
      notifier.notify(new NetworkErrorNotification(e), project);
      return false;
    }
  }

  /**
   * Tries importing IDE settings from the given course. Shows a notification to the user if a
   * network error occurs.
   *
   * @return True if IDE settings were successfully imported, false otherwise.
   */
  private boolean tryImportIdeSettings(@NotNull Project project, @NotNull Course course) {
    try {
      settingsImporter.importIdeSettings(course);
      settingsImporter.importVMOptions(course);
      logger.info("Imported IDE settings");
      return true;
    } catch (IOException e) {
      logger.warn("Failed to import IDE settings", e);
      notifier.notify(new NetworkErrorNotification(e), project);
      return false;
    }
  }

  private boolean tryImportCustomProperties(@NotNull Project project, @NotNull Path basePath,
                                            @NotNull Course course) {
    try {
      settingsImporter.importCustomProperties(basePath, course, project);
      return true;
    } catch (IOException e) {
      logger.warn("Failed to import custom properties", e);
      notifier.notify(new NetworkErrorNotification(e), project);
      return false;
    }
  }

  private void tryImportFeedbackCss(@NotNull Project project,
                                    @NotNull Course course) {
    try {
      settingsImporter.importFeedbackCss(project, course);
    } catch (IOException e) {
      logger.warn("Failed to import custom properties", e);
      notifier.notify(new NetworkErrorNotification(e), project);
    }
  }
}
