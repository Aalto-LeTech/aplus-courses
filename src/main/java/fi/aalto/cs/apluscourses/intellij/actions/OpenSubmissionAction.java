package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionRenderingErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import java.awt.Desktop;
import java.net.URI;
import org.jetbrains.annotations.NotNull;

public class OpenSubmissionAction extends DumbAwareAction {

  public static final String ACTION_ID = OpenSubmissionAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Notifier notifier;

  @NotNull
  private final SubmissionRenderer submissionRenderer;

  /**
   * Construct an {@link OpenSubmissionAction} instance with the given parameters. This constructor
   * is mainly useful for testing purposes.
   */
  public OpenSubmissionAction(@NotNull MainViewModelProvider mainViewModelProvider,
                              @NotNull Notifier notifier,
                              @NotNull SubmissionRenderer submissionRenderer) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.notifier = notifier;
    this.submissionRenderer = submissionRenderer;
  }

  /**
   * Construct an {@link OpenSubmissionAction} instance with reasonable defaults.
   */
  public OpenSubmissionAction() {
    this(
        PluginSettings.getInstance(),
        Notifications.Bus::notify,
        submission -> Desktop.getDesktop().browse(new URI(submission.getUrl()))
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

    SubmissionResultViewModel submission = exercisesTree.getSelectedSubmission();
    if (submission == null) {
      return;
    }

    try {
      submissionRenderer.show(submission.getModel());
    } catch (Exception ex) {
      notifier.notify(new SubmissionRenderingErrorNotification(ex), e.getProject());
    }
  }

  @FunctionalInterface
  public interface SubmissionRenderer {
    void show(@NotNull SubmissionResult submission) throws Exception;
  }

}
