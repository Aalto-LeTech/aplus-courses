package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.ide.startup.StartupActionScriptManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.updateSettings.impl.UpdateSettings;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.ui.IntelliJDialogs;
import fi.aalto.cs.apluscourses.ui.base.Dialogs;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class ImportIdeSettingsAction extends AnAction implements DumbAware {
  @NotNull
  private MainViewModelProvider mainViewModelProvider;

  @NotNull IdeSettingsImporter ideSettingsImporter;

  @NotNull
  private Dialogs dialogs;

  @NotNull
  private IdeRestarter ideRestarter;

  /**
   * Constructs an action using the given main view model provider, ide settings importer, dialog
   * helper, and IDE restarter.
   */
  public ImportIdeSettingsAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                 @NotNull IdeSettingsImporter ideSettingsImporter,
                                 @NotNull Dialogs dialogs,
                                 @NotNull IdeRestarter ideRestarter) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.ideSettingsImporter = ideSettingsImporter;
    this.dialogs = dialogs;
    this.ideRestarter = ideRestarter;
  }

  public ImportIdeSettingsAction() {
    this(PluginSettings.getInstance(), ImportIdeSettingsAction::doImport, new IntelliJDialogs(),
        () -> ((ApplicationEx) ApplicationManager.getApplication()).restart(true));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    CourseViewModel courseViewModel = mainViewModelProvider
        .getMainViewModel(e.getProject())
        .courseViewModel
        .get();
    if (courseViewModel == null) {
      return;
    }
    Course course = courseViewModel.getModel();

    URL ideSettingsUrl = course.getResourceUrls().get("ideSettings");
    if (ideSettingsUrl == null) {
      informNoSettings(course);
      return;
    }
    if (userCancelsImport()) {
      return;
    }
    try {
      ideSettingsImporter.doImport(ideSettingsUrl);
      if (userWantsToRestart()) {
        ideRestarter.restart();
      }
    } catch (IOException | UnexpectedResponseException ex) {
      informErrorOccurred();
    }
  }

  private static void doImport(@NotNull URL ideSettingsUrl)
      throws IOException, UnexpectedResponseException {
    File file = FileUtilRt.createTempFile("course-ide-settings", ".zip");
    CoursesClient.fetchZip(ideSettingsUrl, file);
    String configPath = FileUtilRt.toSystemIndependentName(PathManager.getConfigPath());
    StartupActionScriptManager.addActionCommands(
        Arrays.asList(
            new StartupActionScriptManager.UnzipCommand(file, new File(configPath)),
            new StartupActionScriptManager.DeleteCommand(file)
        )
    );
    UpdateSettings.getInstance().forceCheckForUpdateAfterRestart();
  }

  @FunctionalInterface
  public interface IdeRestarter {
    void restart();
  }

  @FunctionalInterface
  public interface IdeSettingsImporter {
    void doImport(URL file) throws IOException, UnexpectedResponseException;
  }

  private boolean userCancelsImport() {
    return !dialogs.showOkCancelDialog("Importing IDE settings replaces the current settings. "
        + "Existing custom settings are overwritten.", "Import IDE Settings",
        "Import IDE Settings", "Cancel");
  }

  private boolean userWantsToRestart() {
    return dialogs.showOkCancelDialog(
        "IDE settings imported succesfully. Restart the IDE now to reload the settings?",
        "Restart Needed", "Yes", "No");
  }

  private void informNoSettings(@NotNull Course course) {
    dialogs.showInformationDialog(
        "The course \"" + course.getName() + "\" does not provide custom IDE settings",
        "No IDE Settings Found");
  }

  private void informErrorOccurred() {
    dialogs.showErrorDialog("An error occurred while importing IDE settings. Please check your "
        + "network connection and try again, or contact the course staff if the issue persists",
        "Import IDE Settings");
  }

}
