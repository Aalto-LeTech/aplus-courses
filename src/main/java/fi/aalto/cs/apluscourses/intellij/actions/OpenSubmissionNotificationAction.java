package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionRenderingErrorNotification;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle;
import org.jetbrains.annotations.NotNull;

public class OpenSubmissionNotificationAction extends NotificationAction {

  @NotNull
  private final SubmissionResult submissionResult;

  @NotNull
  private final UrlRenderer submissionRenderer;

  @NotNull
  private final Notifier notifier;

  public OpenSubmissionNotificationAction(@NotNull SubmissionResult submissionResult) {
    this(submissionResult, new UrlRenderer(), new DefaultNotifier());
  }

  /**
   * Construct the action with the given parameters. This is mostly useful for testing.
   */
  public OpenSubmissionNotificationAction(@NotNull SubmissionResult submissionResult,
                                          @NotNull UrlRenderer submissionRenderer,
                                          @NotNull Notifier notifier) {
    super(PluginResourceBundle.getText("notification.OpenSubmissionNotificationAction.content"));
    this.submissionResult = submissionResult;
    this.submissionRenderer = submissionRenderer;
    this.notifier = notifier;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    try {
      submissionRenderer.show(submissionResult.getUrl());
    } catch (Exception ex) {
      notifier.notify(new SubmissionRenderingErrorNotification(ex), e.getProject());
    }
  }
}
