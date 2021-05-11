package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class LogInOutAction extends DumbAwareAction {
  private final CourseProjectProvider courseProjectProvider;

  public LogInOutAction() {
    this(PluginSettings.getInstance()::getCourseProject);
  }

  public LogInOutAction(CourseProjectProvider courseProjectProvider) {
    this.courseProjectProvider = courseProjectProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (isLoggedIn(e)) {
      var project = courseProjectProvider.getCourseProject(e.getProject());
      if (project != null && project.getAuthentication() != null) {
        project.setAuthentication(null);
        project.removePasswordFromStorage();
        project.getExercisesUpdater().restart();
      }
    } else {
      ActionUtil.launch(APlusAuthenticationAction.ACTION_ID, e.getDataContext());
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var text = isLoggedIn(e) ? "Log Out" : "Log In";
    e.getPresentation().setText(text);
  }

  private boolean isLoggedIn(@NotNull AnActionEvent e) {
    var project = courseProjectProvider.getCourseProject(e.getProject());
    if (project != null) {
      return !project.getUserName().equals("");
    } else {
      return false;
    }
  }
}
