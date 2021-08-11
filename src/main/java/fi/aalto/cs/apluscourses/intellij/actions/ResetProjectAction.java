package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.ProjectManager;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Component;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

class ResetProjectAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getProject();
    if (project == null) {
      return;
    }

    if (JOptionPane.showConfirmDialog(null,
        getText("ui.ResetProjectAction.confirmDialog.content"),
        getText("ui.ResetProjectAction.confirmDialog.title"),
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

      PluginSettings.getInstance().getCourseFileManager(project).delete();

      var courseProject = PluginSettings.getInstance().getCourseProject(project);

      if (courseProject != null) {
        courseProject.getCourse().getModules().forEach(Component::unload);
      }

      ProjectManager.getInstance().reloadProject(project);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var project = e.getProject();
    if (project == null) {
      return;
    }

    e.getPresentation().setEnabled(PluginSettings.getInstance().getCourseFileManager(project).courseFileExists());
  }
}
