package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import org.jetbrains.annotations.NotNull;

public class RefreshAction extends DumbAwareAction {

  private final MainViewModelProvider mainViewModelProvider;

  public RefreshAction() {
    this(PluginSettings.getInstance());
  }

  public RefreshAction(MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    PluginSettings.getInstance().updateMainViewModel(e.getProject());
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    ExercisesTreeViewModel exercisesTreeViewModel =
            mainViewModelProvider.getMainViewModel(project).exercisesViewModel.get();
    e.getPresentation().setEnabled(exercisesTreeViewModel != null
            && exercisesTreeViewModel.isAuthenticated());
  }
}
