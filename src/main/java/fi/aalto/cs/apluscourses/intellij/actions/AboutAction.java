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
    new AboutDialog(e.getProject()).show();
  }
}
