package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionRenderingErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import org.jetbrains.annotations.NotNull;

public class OpenSubmissionAction extends DumbAwareAction {

  public static final String ACTION_ID = OpenSubmissionAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final UrlRenderer submissionRenderer;

  @NotNull
  private final Notifier notifier;

  /**
   * Construct an {@link OpenSubmissionAction} instance with the given parameters. This constructor
   * is mainly useful for testing purposes.
   */
  public OpenSubmissionAction(@NotNull MainViewModelProvider mainViewModelProvider,
                              @NotNull UrlRenderer submissionRenderer,
                              @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.submissionRenderer = submissionRenderer;
    this.notifier = notifier;
  }

  /**
   * Construct an {@link OpenSubmissionAction} instance with reasonable defaults.
   */
  public OpenSubmissionAction() {
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

    SubmissionResultViewModel submission =
        (SubmissionResultViewModel) exercisesTree.findSelected().getLevel(3);
    if (submission == null) {
      return;
    }

    try {
      submissionRenderer.show(submission.getModel().getUrl());
    } catch (Exception ex) {
      notifier.notify(new SubmissionRenderingErrorNotification(ex), e.getProject());
    }
  }

}
