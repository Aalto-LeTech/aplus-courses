package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class ReadAllNewsAction extends DumbAwareAction {
  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  protected ReadAllNewsAction(@NotNull MainViewModelProvider mainViewModelProvider) {

    this.mainViewModelProvider = mainViewModelProvider;
  }

  /**
   * Construct an {@link OpenItemAction} instance with reasonable defaults.
   */
  protected ReadAllNewsAction() {
    this(
        PluginSettings.getInstance()
    );
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getProject();
    var mainViewModel = mainViewModelProvider.getMainViewModel(project);
    var newsViewModel = mainViewModel.newsTreeViewModel.get();
    if (newsViewModel == null) {
      return;
    }
    newsViewModel.getModel().setAllRead();
    mainViewModel.newsTreeViewModel.valueChanged();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var project = e.getProject();
    var mainViewModel = mainViewModelProvider.getMainViewModel(project);
    e.getPresentation().setEnabled(mainViewModel.newsTreeViewModel.get() != null);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
}
