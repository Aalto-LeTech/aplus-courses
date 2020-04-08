package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.SettingsUtil;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import java.io.IOException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImportProjectSettingsAction extends AnAction {
  @NotNull
  private MainViewModelProvider mainViewModelProvider;

  public ImportProjectSettingsAction(@NotNull MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  public ImportProjectSettingsAction() {
    this(PluginSettings.getInstance());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      // TODO: override AnAction#update instead and only enable this action if a project is loaded
      informNoProjectOpen();
      return;
    }

    CourseViewModel courseViewModel = mainViewModelProvider
        .getMainViewModel(project)
        .courseViewModel
        .get();
    if (courseViewModel == null) {
      return;
    }
    Course course = courseViewModel.getModel();

    URL settingsUrl = course.getResourceUrls().get("projectSettings");
    if (settingsUrl == null) {
      informNoSettings(e.getProject(), course);
      return;
    }

    try {
      SettingsUtil.importProjectSettings(project, settingsUrl);
    } catch (IOException | UnexpectedResponseException ex) {
      informErrorOccurred(project);
    }
  }

  private static void informNoProjectOpen() {
    Messages.showInfoMessage("A project must be loaded before project settings are imported.",
        "No Project Currently Loaded");
  }

  private static void informNoSettings(@Nullable Project project, @NotNull Course course) {
    Messages.showInfoMessage(
        project,
        "The course \"" + course.getName() + "\" does not provide custom project settings",
        "No Project Settings Found");
  }

  private static void informErrorOccurred(@Nullable Project project) {
    Messages.showErrorDialog(project,"An error occurred while importing project settings. Please "
        + "check your network connection and try again, or contact the course staff if the issue "
        + "persists.", "Import Project Settings");
  }
}
