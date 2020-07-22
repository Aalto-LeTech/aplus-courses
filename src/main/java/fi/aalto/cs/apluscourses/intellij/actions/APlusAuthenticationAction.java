package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.APlusExerciseDataSource;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import org.jetbrains.annotations.NotNull;

public class APlusAuthenticationAction extends DumbAwareAction {

  private final MainViewModelProvider mainViewModelProvider;

  private final Dialogs dialogs;

  public APlusAuthenticationAction() {
    this(PluginSettings.getInstance(), Dialogs.DEFAULT);
  }

  public APlusAuthenticationAction(MainViewModelProvider mainViewModelProvider, Dialogs dialogs) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.dialogs = dialogs;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    AuthenticationViewModel authentication = new AuthenticationViewModel();
    if (dialogs.create(authentication, project).showAndGet()) {
      mainViewModel.setExerciseDataSource(APlusExerciseDataSource::new, authentication::build);
    }
  }

  public MainViewModelProvider getMainViewModelProvider() {
    return mainViewModelProvider;
  }

  public Dialogs getDialogs() {
    return dialogs;
  }
}
