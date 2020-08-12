package fi.aalto.cs.apluscourses.intellij.services;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS;
import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notifications;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import fi.aalto.cs.apluscourses.intellij.dal.IntelliJPasswordStorage;
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

  public enum LocalIdeSettingsNames {
    A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG("A+.showReplConfigDialog"),
    A_PLUS_IMPORTED_IDE_SETTINGS("A+.importedIdeSettings");
    private final String name;

    LocalIdeSettingsNames(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static final String COURSE_CONFIGURATION_FILE_URL
      = "https://grader.cs.hut.fi/static/O1_2020/projects/o1_course_config.json";

  public static final String MODULE_REPL_INITIAL_COMMANDS_FILE_NAME
      = ".repl-commands";

  public static final String A_PLUS = "A+";

  //  15 minutes in milliseconds
  public static final long MAIN_VIEW_MODEL_UPDATE_INTERVAL = 15L * 60L * 1000L;
  //  15 seconds in milliseconds
  public static final long REASONABLE_DELAY_FOR_MODULE_INSTALLATION = 15L * 1000;

  private final PropertiesComponent applicationPropertiesManager = PropertiesComponent
      .getInstance();

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
      MainViewModel mainViewModel = mainViewModels.remove(project);
      if (mainViewModel != null) {
        mainViewModel.dispose();
      }
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
   * Triggers a main view model update for the main view model corresponding to the given project.
   * If the project is null, this method does nothing.
   */
  public void updateMainViewModel(@Nullable Project project) {
    if (project == null) {
      return;
    }
    MainViewModelUpdater updater = mainViewModelUpdaters.get(project);
    if (updater != null) {
      updater.restart();
    }
  }

  /**
   * Creates a main view model and launches an updater for it that runs on a background thread.
   *
   * @param project The project to which the created main view model corresponds.
   */
  public void createUpdatingMainViewModel(@NotNull Project project) {
    MainViewModel mainViewModel
        = mainViewModels.computeIfAbsent(project, this::createNewMainViewModel);
    mainViewModelUpdaters.computeIfAbsent(project, p -> {
      MainViewModelUpdater mainViewModelUpdater
          = new MainViewModelUpdater(mainViewModel, p, MAIN_VIEW_MODEL_UPDATE_INTERVAL,
          Notifications.Bus::notify, IntelliJPasswordStorage::new);
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
        applicationPropertiesManager.getValue(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
  }

  /**
   * Method (setter) to set property, responsible for showing REPL configuration window.
   *
   * @param showReplConfigDialog a boolean value of the flag.
   */
  public void setShowReplConfigurationDialog(boolean showReplConfigDialog) {
    applicationPropertiesManager
        //  a String explicitly
        .setValue(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName(),
            String.valueOf(showReplConfigDialog));
  }

  public String getImportedIdeSettingsId() {
    return applicationPropertiesManager.getValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName());
  }

  public void setImportedIdeSettingsId(@NotNull String courseId) {
    applicationPropertiesManager.setValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName(), courseId);
  }

  /**
   * Sets unset local IDE settings to their default values.
   */
  public void initializeLocalIdeSettings() {
    if (!applicationPropertiesManager.isValueSet(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName())) {
      setShowReplConfigurationDialog(true);
    }
    if (!applicationPropertiesManager.isValueSet(A_PLUS_IMPORTED_IDE_SETTINGS.getName())) {
      setImportedIdeSettingsId("");
    }
  }

  /**
   * Resets all local settings to their default values.
   */
  public void resetLocalSettings() {
    unsetLocalIdeSettings();
    initializeLocalIdeSettings();
  }

  /**
   * Unsets all the local IDE settings from {@link LocalIdeSettingsNames}.
   */
  public void unsetLocalIdeSettings() {
    Arrays.stream(LocalIdeSettingsNames.values())
        .map(LocalIdeSettingsNames::getName)
        .forEach(applicationPropertiesManager::unsetValue);
  }

  /**
   * Method that adds a new file (pattern) to the list of files not being shown in the Project UI.
   *
   * @param ignoredFileName a {@link String} name of the file to be ignored.
   * @param project a {@link Project} to ignore the file from.
   */
  public void ignoreFileInProjectView(@NotNull String ignoredFileName,
                                      @NotNull Project project) {
    FileTypeManager fileTypeManager = FileTypeManager.getInstance();
    String ignoredFilesList = fileTypeManager.getIgnoredFilesList();
    Runnable runnable = () -> fileTypeManager
        .setIgnoredFilesList(ignoredFilesList + ignoredFileName + ";");
    WriteCommandAction.runWriteCommandAction(project, runnable);
  }

}
