package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.actions.OpenSubmissionNotificationAction;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import org.jetbrains.annotations.NotNull;

public class FeedbackAvailableNotification extends Notification {

  /**
   * Construct a notification that notifies the user that feedback is available for a submission.
   * The notification contains a link that can be used to open the feedback.
   */
  public FeedbackAvailableNotification(@NotNull SubmissionResult submissionResult,
                                       @NotNull String exerciseName) {
    super(
        "A+",
        "Submission feedback available",
        "Feedback for '" + exerciseName + "' is now available. "
            + "Click the link below to open the feedback.",
        NotificationType.INFORMATION
    );
    super.addAction(new OpenSubmissionNotificationAction(submissionResult));
  }

}
