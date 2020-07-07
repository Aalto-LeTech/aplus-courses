package fi.aalto.cs.apluscourses.intellij.services;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS;
import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModelUpdater;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginSettings implements MainViewModelProvider {

  private static final PluginSettings instance = new PluginSettings();

  private PluginSettings() {

  }

  public enum LocalSettingsNames {
    A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG("A+.showReplConfigDialog"),
    A_PLUS_IMPORTED_IDE_SETTINGS("A+.importedIdeSettings");
    private final String name;

    LocalSettingsNames(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

  public static final String COURSE_CONFIGURATION_FILE_URL
      = "https://grader.cs.hut.fi/static/O1_2020/projects/o1_course_config.json";

  public static final String A_PLUS_API_BASE_URL = "https://plus.cs.aalto.fi/api/v2";

  //  15 minutes in milliseconds
  public static final long MAIN_VIEW_MODEL_UPDATE_INTERVAL = 15L * 60L * 1000L;
  //  10 minutes in milliseconds
  public static final long REASONABLE_DELAY_FOR_SUBMISSION_RESULTS_UPDATE = 10L * 60 * 1000;
  //  15 seconds in milliseconds
  public static final long REASONABLE_DELAY_FOR_MODULE_INSTALLATION = 15L * 1000;

  private final PropertiesComponent propertiesManager = PropertiesComponent.getInstance();

  @NotNull
  private final ConcurrentMap<Project, MainViewModel> mainViewModels = new ConcurrentHashMap<>();

  @NotNull
  private final ConcurrentMap<Project, MainViewModelUpdater> mainViewModelUpdaters
      = new ConcurrentHashMap<>();

  private final ProjectManagerListener projectManagerListener = new ProjectManagerListener() {
    @Override
    public void projectClosed(@NotNull Project project) {
      MainViewModelUpdater updater = mainViewModelUpdaters.remove(project);
      if (updater != null) {
        updater.interrupt();
      }
      mainViewModels.remove(project);
      ProjectManager.getInstance().removeProjectManagerListener(project, this);
    }
  };

  /**
   * Methods to get the Singleton instance of {@link PluginSettings}.
   *
   * @return an instance of {@link PluginSettings}.
   */
  @NotNull
  public static PluginSettings getInstance() {
    return instance;
  }

  /**
   * Returns a MainViewModel for a specific project.
   *
   * @param project A project, or null (equivalent for default project).
   * @return A main view model.
   */
  @NotNull
  public MainViewModel getMainViewModel(@Nullable Project project) {
    if (project == null || !project.isOpen()) {
      // If project is closed, use default project to avoid creation of main view models, that would
      // never be cleaned up.
      project = ProjectManager.getInstance().getDefaultProject();
    }
    return mainViewModels.computeIfAbsent(project, this::createNewMainViewModel);
  }

  /**
   * Creates a main view model and launches an updater for it that runs on a background thread.
   *
   * @param project The project to which the created main view model corresponds.
   */
  @NotNull
  public void createUpdatingMainViewModel(@NotNull Project project) {
    MainViewModel mainViewModel
        = mainViewModels.computeIfAbsent(project, this::createNewMainViewModel);
    mainViewModelUpdaters.computeIfAbsent(project, p -> {
      MainViewModelUpdater mainViewModelUpdater
          = new MainViewModelUpdater(mainViewModel, p, MAIN_VIEW_MODEL_UPDATE_INTERVAL,
          Notifications.Bus::notify);
      mainViewModelUpdater.start();
      return mainViewModelUpdater;
    });
  }

  @NotNull
  private MainViewModel createNewMainViewModel(@NotNull Project project) {
    ProjectManager.getInstance().addProjectManagerListener(project, projectManagerListener);
    return new MainViewModel();
  }

  /**
   * Method (getter) to check the property, responsible for showing REPL configuration window.
   */
  public boolean shouldShowReplConfigurationDialog() {
    return Boolean.parseBoolean(
        propertiesManager.getValue(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
  }

  /**
   * Method (setter) to set property, responsible for showing REPL configuration window.
   *
   * @param showReplConfigDialog a boolean value of the flag.
   */
  public void setShowReplConfigurationDialog(boolean showReplConfigDialog) {
    propertiesManager
        //  a String explicitly
        .setValue(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName(),
            String.valueOf(showReplConfigDialog));
  }

  public String getImportedIdeSettingsId() {
    return propertiesManager.getValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName());
  }

  public void setImportedIdeSettingsId(@NotNull String courseId) {
    propertiesManager.setValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName(), courseId);
  }

  /**
   * Sets unset local settings to their default values.
   */
  public void initializeLocalSettings() {
    if (!propertiesManager.isValueSet(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName())) {
      setShowReplConfigurationDialog(true);
    }
    if (!propertiesManager.isValueSet(A_PLUS_IMPORTED_IDE_SETTINGS.getName())) {
      setImportedIdeSettingsId("");
    }
  }

  /**
   * Resets all local settings to their default values.
   */
  public void resetLocalSettings() {
    unsetLocalSettings();
    initializeLocalSettings();
  }

  /**
   * Unsets all the local settings from {@link LocalSettingsNames}.
   */
  public void unsetLocalSettings() {
    Arrays.stream(LocalSettingsNames.values())
        .map(LocalSettingsNames::getName)
        .forEach(propertiesManager::unsetValue);
  }
}
