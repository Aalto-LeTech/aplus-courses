package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.intellij.model.IdeSettingsImporter;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import java.io.IOException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImportIdeSettingsAction extends AnAction implements DumbAware {
  @NotNull
  private MainViewModelProvider mainViewModelProvider;

  public ImportIdeSettingsAction(@NotNull MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  public ImportIdeSettingsAction() {
    this(PluginSettings.getInstance());
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
      informNoSettings(e.getProject(), course);
      return;
    }
    if (userCancelsImport(e.getProject())) {
      return;
    }
    try {
      IdeSettingsImporter.importFromUrl(ideSettingsUrl);
      if (userWantsToRestart(e.getProject())) {
        restart();
      }
    } catch (IOException | UnexpectedResponseException ex) {
      informErrorOccurred(e.getProject());
    }
  }

  private static boolean userCancelsImport(@Nullable Project project) {
    int answer = Messages.showOkCancelDialog(project, "Importing IDE settings replaces the "
        + " current settings. Existing custom settings are overwritten.", "Import IDE Settings",
        "Import IDE Settings", "Cancel", Messages.getQuestionIcon());
    return answer != Messages.OK;
  }

  private static boolean userWantsToRestart(@Nullable Project project) {
    int answer = Messages.showYesNoDialog(project,
        "IDE settings imported succesfully. Restart the IDE now to reload the settings?",
        "Restart Needed", Messages.getQuestionIcon());
    return answer == Messages.YES;
  }

  private static void informNoSettings(@Nullable Project project, @NotNull Course course) {
    Messages.showInfoMessage(
        project,
        "The course \"" + course.getName() + "\" does not provide custom IDE settings",
        "No IDE Settings Found");
  }

  private static void informErrorOccurred(@Nullable Project project) {
    Messages.showErrorDialog(project,"An error occurred while importing IDE settings. Please "
        + "check your network connection and try again, or contact the course staff if the issue "
        + "persists.", "Import IDE Settings");
  }

  private static void restart() {
    ApplicationManager.getApplication().invokeLater(
        () -> ((ApplicationEx) ApplicationManager.getApplication()).restart(true));
  }

}
