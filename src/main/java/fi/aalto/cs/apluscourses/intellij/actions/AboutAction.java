package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.model.CommonLibraryProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.ui.AboutDialog;
import org.jetbrains.annotations.NotNull;

public class AboutAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (e.getProject() == null) {
      return;
    }
    var project = e.getProject();
    var course =  PluginSettings.getInstance().getCourseProject(project).getCourse();
    boolean updated = course.getModules().stream().anyMatch(Module::updateScalaVersionForO1);
//    var sdkComponent = new CommonLibraryProvider(project).getComponentIfExists("scala-sdk-3.2.0");
    course.validate();
//    new AboutDialog(e.getProject()).show();
  }
}
