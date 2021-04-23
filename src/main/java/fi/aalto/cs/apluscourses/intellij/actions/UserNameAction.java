package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class UserNameAction extends AnAction {
  private final MainViewModelProvider mainViewModelProvider;

  public UserNameAction() {
    this(PluginSettings.getInstance());
  }

  public UserNameAction(MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    // Does nothing.
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(false);
    var userName = mainViewModelProvider.getMainViewModel(e.getProject()).getUserName();
    userName = userName.equals("") ? "Not Logged In" : userName;
    e.getPresentation().setText(userName);
  }
}
