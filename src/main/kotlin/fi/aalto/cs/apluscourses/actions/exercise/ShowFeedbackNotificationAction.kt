package fi.aalto.cs.apluscourses.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult;
import org.jetbrains.annotations.NotNull;

public class ShowFeedbackNotificationAction extends NotificationAction {
  private final SubmissionResult submissionResult;

  public ShowFeedbackNotificationAction(SubmissionResult submissionResult) {
    super(getText("notification.ShowFeedbackNotificationAction.content"));
    this.submissionResult = submissionResult;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
//    ShowFeedbackAction action =
//        (ShowFeedbackAction) ActionManager.getInstance().getAction(ShowFeedbackAction.ACTION_ID);
//    action.setSubmissionResult(submissionResult);
//    action.actionPerformed(e);
  }
}
