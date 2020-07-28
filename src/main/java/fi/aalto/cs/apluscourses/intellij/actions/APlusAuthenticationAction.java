package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.APlusExerciseDataSource;
import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.intellij.dal.IntelliJPasswordStorage;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import org.jetbrains.annotations.NotNull;

public class APlusAuthenticationAction extends DumbAwareAction {

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final PasswordStorage.Factory passwordStorageFactory;

  @NotNull
  private final Dialogs dialogs;


  public APlusAuthenticationAction() {
    this(PluginSettings.getInstance(), IntelliJPasswordStorage::new, Dialogs.DEFAULT);
  }

  /**
   * Constructs a new {@link APlusAuthenticationAction}.
   *
   * @param mainViewModelProvider  Main view model provider.
   * @param passwordStorageFactory Password storage factory.
   * @param dialogs                Dialogs to be used within this action.
   */
  public APlusAuthenticationAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                   @NotNull PasswordStorage.Factory passwordStorageFactory,
                                   @NotNull Dialogs dialogs) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.passwordStorageFactory = passwordStorageFactory;
    this.dialogs = dialogs;
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
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    if (courseViewModel == null) {
      return;
    }
    String apiUrl = courseViewModel.getModel().getApiUrl();
    PasswordStorage passwordStorage = passwordStorageFactory.create(apiUrl);
    AuthenticationViewModel authenticationViewModel =
        new AuthenticationViewModel(APlusTokenAuthentication.getFactoryFor(passwordStorage));
    if (dialogs.create(authenticationViewModel, project).showAndGet()) {
      Authentication authentication = authenticationViewModel.build();
      authentication.persist();
      mainViewModel.disposing.addListener(authentication, Authentication::clear);
      mainViewModel.exerciseDataSource.set(new APlusExerciseDataSource(authentication, apiUrl));
    }
  }

  @NotNull
  public MainViewModelProvider getMainViewModelProvider() {
    return mainViewModelProvider;
  }

  @NotNull
  public Dialogs getDialogs() {
    return dialogs;
  }
}
