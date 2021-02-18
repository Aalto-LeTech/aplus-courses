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
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class APlusAuthenticationAction extends DumbAwareAction {

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Dialogs dialogs;

  @NotNull
  private final PasswordStorage.Factory passwordStorageFactory;

  @NotNull
  private final Notifier notifier;

  /**
   * Called by th platform.
   */
  public APlusAuthenticationAction() {
    this(PluginSettings.getInstance(),
        Dialogs.DEFAULT,
        IntelliJPasswordStorage::new,
        new DefaultNotifier());
  }

  /**
   * Constructor for testing.
   */
  public APlusAuthenticationAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                   @NotNull Dialogs dialogs,
                                   @NotNull PasswordStorage.Factory passwordStorageFactory,
                                   @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.dialogs = dialogs;
    this.passwordStorageFactory = passwordStorageFactory;
    this.notifier = notifier;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    CourseViewModel courseViewModel =
        mainViewModelProvider.getMainViewModel(e.getProject()).courseViewModel.get();
    e.getPresentation().setEnabled(courseViewModel != null);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    Course course;
    var courseViewModel = mainViewModel.courseViewModel.get();
    if (courseViewModel != null) {
      course = courseViewModel.getModel();
    } else {
      return;
    }

    String apiUrl = course.getApiUrl();
    String authenticationHtmlUrl = course.getHtmlUrl() + "accounts/accounts/";

    PasswordStorage passwordStorage = passwordStorageFactory.create(apiUrl);
    AuthenticationViewModel authenticationViewModel = new AuthenticationViewModel(
        APlusTokenAuthentication.getFactoryFor(passwordStorage),
        authenticationHtmlUrl
    );

    if (!dialogs.create(authenticationViewModel, project).showAndGet()) {
      return;
    }

    Authentication authentication = authenticationViewModel.build();
    if (!authentication.persist()) {
      notifier.notify(new ApiTokenNotSetNotification(), project);
    }
    mainViewModel.setAuthentication(authentication);
  }

  @NotNull
  public MainViewModelProvider getMainViewModelProvider() {
    return mainViewModelProvider;
  }

  @NotNull
  public Dialogs getDialogs() {
    return dialogs;
  }

  @NotNull
  public PasswordStorage.Factory getPasswordStorageFactory() {
    return passwordStorageFactory;
  }
}
