package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.actions.OpenSubmissionNotificationAction;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import org.jetbrains.annotations.NotNull;

public class FeedbackAvailableNotification extends Notification {

  /**
   * Construct a notification that notifies the user that feedback is available for a submission.
   * The notification contains a link that can be used to open the feedback and the amount of
   * points, or the test results the submission got.
   */
  public FeedbackAvailableNotification(@NotNull SubmissionResult submissionResult,
                                       @NotNull Exercise exercise) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.FeedbackAvailableNotification.title"),
        getAndReplaceText("notification.FeedbackAvailableNotification.content",
            APlusLocalizationUtil.getEnglishName(exercise.getName()),
            submissionResult.getFeedbackString()),
        NotificationType.INFORMATION
    );
    super.addAction(new OpenSubmissionNotificationAction(submissionResult));
  }

}
