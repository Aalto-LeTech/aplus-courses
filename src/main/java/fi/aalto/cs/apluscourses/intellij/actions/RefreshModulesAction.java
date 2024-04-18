package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class RefreshModulesAction extends DumbAwareAction {

  @NotNull
  private final CourseProjectProvider courseProjectProvider;

  public RefreshModulesAction(@NotNull CourseProjectProvider courseProjectProvider) {
    this.courseProjectProvider = courseProjectProvider;
  }

  public RefreshModulesAction() {
    this(PluginSettings.getInstance()::getCourseProject);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var project = e.getProject();
    e.getPresentation().setEnabled(
        project != null && courseProjectProvider.getCourseProject(project) != null);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var courseProject = courseProjectProvider.getCourseProject(e.getProject());
    if (courseProject == null) {
      return;
    }
    courseProject.courseUpdater.restart();
  }
}
