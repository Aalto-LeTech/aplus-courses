package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.UrlRenderingErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Browsable;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import org.jetbrains.annotations.NotNull;

public class OpenItemAction extends DumbAwareAction {

  public static final String ACTION_ID = OpenItemAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final UrlRenderer urlRenderer;

  @NotNull
  private final Notifier notifier;

  /**
   * Construct an {@link OpenItemAction} instance with the given parameters. This constructor
   * is mainly useful for testing purposes.
   */
  public OpenItemAction(@NotNull MainViewModelProvider mainViewModelProvider,
                        @NotNull UrlRenderer urlRenderer,
                        @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.urlRenderer = urlRenderer;
    this.notifier = notifier;
  }

  /**
   * Construct an {@link OpenItemAction} instance with reasonable defaults.
   */
  public OpenItemAction() {
    this(
        PluginSettings.getInstance(),
        new UrlRenderer(),
        new DefaultNotifier()
    );
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    ExercisesTreeViewModel exercisesTree = mainViewModelProvider
        .getMainViewModel(e.getProject())
        .exercisesViewModel
        .get();
    if (exercisesTree == null) {
      return;
    }

    SelectableNodeViewModel<?> nodeViewModel = exercisesTree.getSelectedItem();
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
    var exercisesTreeViewModel =
            mainViewModelProvider.getMainViewModel(project).exercisesViewModel.get();
    e.getPresentation().setEnabled(exercisesTreeViewModel != null
            && exercisesTreeViewModel.isAuthenticated());
  }
}
