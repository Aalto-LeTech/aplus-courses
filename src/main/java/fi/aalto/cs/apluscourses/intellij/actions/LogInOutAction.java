package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenStorage;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class LogInOutAction extends DumbAwareAction {
  @NotNull
  private final CourseProjectProvider courseProjectProvider;

//  @NotNull
//  private final PasswordStorage.Factory passwordStorageFactory;


  public LogInOutAction() {
    this(PluginSettings.getInstance()::getCourseProject//,
//        TokenStorage::new
    );
  }

  public LogInOutAction(@NotNull CourseProjectProvider courseProjectProvider//,
//                        @NotNull PasswordStorage.Factory passwordStorageFactory
  ) {
    this.courseProjectProvider = courseProjectProvider;
//    this.passwordStorageFactory = passwordStorageFactory;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (isLoggedIn(e)) {
      var project = courseProjectProvider.getCourseProject(e.getProject());
      if (project != null && project.getAuthentication() != null) {
        project.setAuthentication(null);
//        project.removePasswordFromStorage(passwordStorageFactory);
//        project.exercisesUpdater.restart(project);
      }
    } else {
      ActionUtil.launch(APlusAuthenticationAction.ACTION_ID, e.getDataContext());
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var text = isLoggedIn(e)
        ? getText("presentation.userDropdown.logOut")
        : getText("presentation.userDropdown.logIn");
    e.getPresentation().setText(text);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private boolean isLoggedIn(@NotNull AnActionEvent e) {
    var project = courseProjectProvider.getCourseProject(e.getProject());
    if (project != null) {
      return !project.getUserName().equals("");
    } else {
      return false;
    }
  }
}
