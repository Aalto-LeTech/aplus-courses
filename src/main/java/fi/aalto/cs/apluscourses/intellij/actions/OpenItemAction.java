package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.UrlRenderingErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Browsable;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenItemAction<T> extends DumbAwareAction {
  @NotNull
  protected final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final UrlRenderer urlRenderer;

  @NotNull
  private final Notifier notifier;

  /**
   * Construct an {@link OpenItemAction} instance with the given parameters. This constructor
   * is mainly useful for testing purposes.
   */
  protected OpenItemAction(@NotNull MainViewModelProvider mainViewModelProvider,
                           @NotNull UrlRenderer urlRenderer,
                           @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.urlRenderer = urlRenderer;
    this.notifier = notifier;
  }

  /**
   * Construct an {@link OpenItemAction} instance with reasonable defaults.
   */
  protected OpenItemAction() {
    this(
        PluginSettings.getInstance(),
        new UrlRenderer(),
        new DefaultNotifier()
    );
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (e.getProject() == null) {
      return;
    }
    BaseTreeViewModel<T> treeViewModel = getTreeViewModel(e.getProject());
    if (treeViewModel == null) {
      return;
    }

    SelectableNodeViewModel<?> nodeViewModel = treeViewModel.getSelectedItem();
    if (nodeViewModel == null) {
      return;
    }

    try {
      urlRenderer.show(((Browsable) nodeViewModel.getModel()).getHtmlUrl());
    } catch (Exception ex) {
      notifier.notify(new UrlRenderingErrorNotification(ex), e.getProject());
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var project = e.getProject();
    e.getPresentation().setEnabled(project != null && getTreeViewModel(project) != null);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Nullable
  BaseTreeViewModel<T> getTreeViewModel(@NotNull Project project) {
    return null;
  }
}
