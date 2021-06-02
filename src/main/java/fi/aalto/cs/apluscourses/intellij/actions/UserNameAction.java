package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class UserNameAction extends AnAction {
  private final CourseProjectProvider courseProjectProvider;

  public UserNameAction() {
    this(PluginSettings.getInstance()::getCourseProject);
  }

  public UserNameAction(CourseProjectProvider courseProjectProvider) {
    this.courseProjectProvider = courseProjectProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    // Does nothing.
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(false);
    var project = courseProjectProvider.getCourseProject(e.getProject());
    if (project != null) {
      var userName = project.getUserName();
      userName = userName.equals("") ? getText("presentation.userDropdown.notLoggedIn") : userName;
      e.getPresentation().setText(userName);
    }
  }
}
