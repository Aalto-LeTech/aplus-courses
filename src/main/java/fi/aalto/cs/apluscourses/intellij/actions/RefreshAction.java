package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class RefreshAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    PluginSettings.getInstance().updateMainViewModel(e.getProject());
  }
}
