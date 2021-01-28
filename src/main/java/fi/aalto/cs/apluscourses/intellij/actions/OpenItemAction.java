package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionRenderingErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import org.jetbrains.annotations.NotNull;

public class OpenItemAction extends DumbAwareAction {

  public static final String ACTION_ID = OpenItemAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final UrlRenderer submissionRenderer;

  @NotNull
  private final Notifier notifier;

  /**
   * Construct an {@link OpenItemAction} instance with the given parameters. This constructor
   * is mainly useful for testing purposes.
   */
  public OpenItemAction(@NotNull MainViewModelProvider mainViewModelProvider,
                        @NotNull UrlRenderer submissionRenderer,
                        @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.submissionRenderer = submissionRenderer;
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

    String url;
    if (nodeViewModel instanceof SubmissionResultViewModel) {
      url = ((SubmissionResultViewModel) nodeViewModel).getModel().getUrl();
    } else if (nodeViewModel instanceof ExerciseViewModel) {
      url = ((ExerciseViewModel) nodeViewModel).getModel().getHtmlUrl();
    } else if (nodeViewModel instanceof ExerciseGroupViewModel) {
      url = ((ExerciseGroupViewModel) nodeViewModel).getModel().getHtmlUrl();
    } else {
      return;
    }

    try {
      submissionRenderer.show(url);
    } catch (Exception ex) {
      notifier.notify(new SubmissionRenderingErrorNotification(ex), e.getProject());
    }
  }

}
