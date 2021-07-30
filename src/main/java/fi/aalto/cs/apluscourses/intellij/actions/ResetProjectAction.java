package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.ProjectManager;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

class ResetProjectAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getProject();
    if (project == null) {
      return;
    }

    if (JOptionPane.showConfirmDialog(null,
        "Are you sure you want to reset the course project?\nThis will remove the current course configuration.",
        "Change Course",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

      PluginSettings.getInstance().getCourseFileManager(project).delete();

      ProjectManager.getInstance().reloadProject(project);
    }
  }
}
