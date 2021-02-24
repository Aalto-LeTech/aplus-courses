package fi.aalto.cs.apluscourses.intellij.services;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_DEFAULT_GROUP;
import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS;
import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.intellij.dal.IntelliJPasswordStorage;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.utils.CourseFileManager;
import fi.aalto.cs.apluscourses.intellij.utils.IntelliJFilterOption;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseFilter;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupFilter;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;

import java.util.Arrays;
import java.util.Optional;
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
    A_PLUS_IMPORTED_IDE_SETTINGS("A+.importedIdeSettings"),
    A_PLUS_DEFAULT_GROUP("A+.defaultGroup"),
    A_PLUS_SHOW_NON_SUBMITTABLE("A+.showNonSubmittable"),
    A_PLUS_SHOW_COMPLETED("A+.showCompleted"),
    A_PLUS_SHOW_OPTIONAL("A+.showOptional"),
    A_PLUS_SHOW_CLOSED("A+.showClosed");

    private final String name;

    LocalIdeSettingsNames(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static final String MODULE_REPL_INITIAL_COMMANDS_FILE_NAME
      = ".repl-commands";

  public static final String A_PLUS = "A+";

  //  15 minutes in milliseconds
  public static final long COURSE_UPDATE_INTERVAL = 15L * 60 * 1000;
  //  15 seconds in milliseconds
  public static final long REASONABLE_DELAY_FOR_MODULE_INSTALLATION = 15L * 1000;

  private final PropertiesComponent applicationPropertiesManager = PropertiesComponent
      .getInstance();

  private static class ProjectKey {
    @NotNull
    private final String projectPath;

    public ProjectKey(@Nullable Project project) {
      if (project == null || project.isDefault()) {
        this.projectPath = "";
      } else {
        this.projectPath = project.getBasePath();
      }
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ProjectKey)) {
        return false;
      }
      return projectPath.equals(((ProjectKey) other).projectPath);
    }

    @Override
    public int hashCode() {
      return projectPath.hashCode();
    }
  }

  @NotNull
  private final ConcurrentMap<ProjectKey, MainViewModel> mainViewModels = new ConcurrentHashMap<>();

  @NotNull
  private final ConcurrentMap<ProjectKey, CourseProject> courseProjects = new ConcurrentHashMap<>();

  @NotNull
  private final ConcurrentMap<ProjectKey, CourseFileManager> courseFileManagers
      = new ConcurrentHashMap<>();

  @NotNull
  private final Options exerciseFilterOptions = new Options(
      new IntelliJFilterOption(LocalIdeSettingsNames.A_PLUS_SHOW_NON_SUBMITTABLE,
          getText("presentation.exerciseFilterOptions.nonSubmittable"),
          null,
          new ExerciseFilter.NonSubmittableFilter()),
      new IntelliJFilterOption(LocalIdeSettingsNames.A_PLUS_SHOW_COMPLETED,
          getText("presentation.exerciseFilterOptions.Completed"),
          null,
          new ExerciseFilter.CompletedFilter()),
      new IntelliJFilterOption(LocalIdeSettingsNames.A_PLUS_SHOW_OPTIONAL,
          getText("presentation.exerciseFilterOptions.Optional"),
          null,
          new ExerciseFilter.OptionalFilter()),
      new IntelliJFilterOption(LocalIdeSettingsNames.A_PLUS_SHOW_CLOSED,
          getText("presentation.exerciseGroupFilterOptions.Closed"),
          null,
          new ExerciseGroupFilter.ClosedFilter()));

  private final ProjectManagerListener projectManagerListener = new ProjectManagerListener() {
    @Override
    public void projectClosed(@NotNull Project project) {
      ProjectKey key = new ProjectKey(project);
      courseFileManagers.remove(key);
      courseProjects.remove(key);
      MainViewModel mainViewModel = mainViewModels.remove(key);
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
    // ProjectKey takes care or project being null and avoids creating differing keys for null.
    ProjectKey key = new ProjectKey(project);
    return mainViewModels.computeIfAbsent(key, projectKey -> {
      ProjectManager
          .getInstance()
          .addProjectManagerListener(project, projectManagerListener);
      return new MainViewModel(exerciseFilterOptions);
    });
  }

  /**
   * Triggers a main view model update for the main view model corresponding to the given project.
   * If the project is null, this method does nothing.
   */
  public void updateMainViewModel(@Nullable Project project) {
    ProjectKey key = new ProjectKey(project);
    var courseProject = courseProjects.get(key);
    if (courseProject != null) {
      courseProject.getCourseUpdater().restart();
    }
  }

  /**
   * Registers a course project. This creates a main view model. It also starts the updater of the
   * course project. Calling this method again with the same project has no effect.
   */
  public void registerCourseProject(@NotNull Project project,
                                    @NotNull CourseProject courseProject) {
    var key = new ProjectKey(project);
    var mainViewModel = getMainViewModel(project);
    courseProjects.computeIfAbsent(key, projectKey -> {
      courseProject.getCourse().register();
      mainViewModel.courseViewModel.set(new CourseViewModel(courseProject.getCourse()));
      courseProject.courseUpdated.addListener(
          mainViewModel.courseViewModel, ObservableProperty::valueChanged);
      courseProject.getCourseUpdater().restart();
      return courseProject;
    });
    var passwordStorage = new IntelliJPasswordStorage(courseProject.getCourse().getApiUrl());
    TokenAuthentication.Factory authenticationFactory =
        APlusTokenAuthentication.getFactoryFor(passwordStorage);
    mainViewModel.readAuthenticationFromStorage(passwordStorage, authenticationFactory);
  }

  /**
   * Returns the {@link CourseFileManager} instance corresponding to the given project. A new
   * instance is created if no instance exists yet.
   */
  @NotNull
  public CourseFileManager getCourseFileManager(@NotNull Project project) {
    return courseFileManagers.computeIfAbsent(
        new ProjectKey(project),
        key -> new CourseFileManager(project)
    );
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

  public Optional<Long> getDefaultGroupId() {
    String id = applicationPropertiesManager.getValue(A_PLUS_DEFAULT_GROUP.getName());
    return Optional.ofNullable(id).map(Long::parseLong);
  }

  public void setDefaultGroupId(long groupId) {
    applicationPropertiesManager.setValue(A_PLUS_DEFAULT_GROUP.getName(), String.valueOf(groupId));
  }

  public void clearDefaultGroupId() {
    applicationPropertiesManager.unsetValue(A_PLUS_DEFAULT_GROUP.getName());
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
    exerciseFilterOptions.forEach(Option::init);
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
