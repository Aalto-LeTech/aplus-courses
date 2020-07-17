package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.APlusAuthenticationView;
import org.jetbrains.annotations.NotNull;

public class APlusAuthenticationAction extends DumbAwareAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    new APlusAuthenticationView(mainViewModel.getAuthentication(), project).show();
  }
}
