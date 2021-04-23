package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class LogInOutAction extends AnAction {
  private final MainViewModelProvider mainViewModelProvider;

  public LogInOutAction() {
    this(PluginSettings.getInstance());
  }

  public LogInOutAction(MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (isLoggedIn(e)) {
      mainViewModelProvider.getMainViewModel(e.getProject()).authentication.set(null);
      mainViewModelProvider.getMainViewModel(e.getProject()).removePasswordFromStorage();
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
    return !mainViewModelProvider.getMainViewModel(e.getProject()).getUserName().equals("");
  }
}
