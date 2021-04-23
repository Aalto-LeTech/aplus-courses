package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

public class UserActionGroup extends DefaultActionGroup implements DumbAware {
  private final MainViewModelProvider mainViewModelProvider;

  public UserActionGroup() {
    this(PluginSettings.getInstance());
  }

  public UserActionGroup(MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var userName = mainViewModelProvider.getMainViewModel(e.getProject()).getUserName();
    var loggedIn = !userName.equals("");
    var icon = loggedIn ? PluginIcons.A_PLUS_USER_LOGGED_IN : PluginIcons.A_PLUS_USER;
    var text = loggedIn ? "Logged in as " + userName : "Not Logged In";
    e.getPresentation().setIcon(icon);
    e.getPresentation().setText(text);
  }
}
