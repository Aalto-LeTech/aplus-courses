package fi.aalto.cs.apluscourses.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.notifications.UrlRenderingErrorNotification;
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult;
import fi.aalto.cs.apluscourses.services.Notifier;
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle;
import org.jetbrains.annotations.NotNull;

public class OpenSubmissionNotificationAction extends NotificationAction {

  @NotNull
  private final SubmissionResult submissionResult;

//  @NotNull
//  private final UrlRenderer submissionRenderer;

//  public OpenSubmissionNotificationAction(@NotNull SubmissionResult submissionResult) {
//    this(submissionResult, new UrlRenderer());
//  }

  /**
   * Construct the action with the given parameters. This is mostly useful for testing.
   */
  public OpenSubmissionNotificationAction(@NotNull SubmissionResult submissionResult
//                                          @NotNull UrlRenderer submissionRenderer
  ) {
    super(PluginResourceBundle.getText("notification.OpenSubmissionNotificationAction.content"));
    this.submissionResult = submissionResult;
//    this.submissionRenderer = submissionRenderer;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    try {
//      submissionRenderer.show(submissionResult.getHtmlUrl());
    } catch (Exception ex) {
      Notifier.Companion.notify(new UrlRenderingErrorNotification(ex), e.getProject());
    }
  }
}
