package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.APlusAuthenticationView;
import org.jetbrains.annotations.NotNull;

public class APlusAuthenticationAction extends DumbAwareAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(e.getProject());
    APlusAuthenticationViewModel authenticationViewModel
        = new APlusAuthenticationViewModel(e.getProject());
    new APlusAuthenticationView(authenticationViewModel).show();
    if (authenticationViewModel.getAuthentication() != null) {
      mainViewModel.authenticationViewModel.set(authenticationViewModel);
    }
  }
}
