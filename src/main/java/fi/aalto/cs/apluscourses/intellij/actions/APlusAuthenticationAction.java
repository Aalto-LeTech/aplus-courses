package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.intellij.dal.IntelliJPasswordStorage;
import fi.aalto.cs.apluscourses.intellij.notifications.ApiTokenNotSetNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import org.jetbrains.annotations.NotNull;

public class APlusAuthenticationAction extends DumbAwareAction {

  public static final String ACTION_ID = APlusAuthenticationAction.class.getCanonicalName();

  @NotNull
  private final CourseProjectProvider courseProjectProvider;

  @NotNull
  private final Dialogs dialogs;

  @NotNull
  private final PasswordStorage.Factory passwordStorageFactory;

  @NotNull
  private final Notifier notifier;

  /**
   * Called by the platform.
   */
  public APlusAuthenticationAction() {
    this(
        PluginSettings.getInstance()::getCourseProject,
        Dialogs.DEFAULT,
        IntelliJPasswordStorage::new,
        new DefaultNotifier()
    );
  }

  /**
   * Constructor for testing.
   */
  public APlusAuthenticationAction(@NotNull CourseProjectProvider courseProjectProvider,
                                   @NotNull Dialogs dialogs,
                                   @NotNull PasswordStorage.Factory passwordStorageFactory,
                                   @NotNull Notifier notifier) {
    this.courseProjectProvider = courseProjectProvider;
    this.dialogs = dialogs;
    this.passwordStorageFactory = passwordStorageFactory;
    this.notifier = notifier;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(courseProjectProvider.getCourseProject(e.getProject()) != null);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    var courseProject = courseProjectProvider.getCourseProject(project);
    if (courseProject == null) {
      return;
    }
    var course = courseProject.getCourse();

    String apiUrl = course.getApiUrl();
    String authenticationHtmlUrl = course.getHtmlUrl() + "accounts/accounts/";

    PasswordStorage passwordStorage = passwordStorageFactory.create(apiUrl);
    AuthenticationViewModel authenticationViewModel = new AuthenticationViewModel(
        APlusTokenAuthentication.getFactoryFor(passwordStorage),
        authenticationHtmlUrl,
        course.getExerciseDataSource()
    );

    if (!dialogs.create(authenticationViewModel, project).showAndGet()) {
      return;
    }

    Authentication authentication = authenticationViewModel.getAuthentication();
    if (authentication == null) {
      return;
    }
    if (!authentication.persist()) {
      notifier.notify(new ApiTokenNotSetNotification(), project);
    }
    courseProject.setAuthentication(authentication);
    courseProject.getExercisesUpdater().restart();
  }

}
