package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.actions.exercise.OpenSubmissionNotificationAction
import fi.aalto.cs.apluscourses.actions.exercise.ShowFeedbackNotificationAction
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.SubmissionResultUtil

//import fi.aalto.cs.apluscourses.services.PluginSettings.Companion.getInstance

class FeedbackAvailableNotification(
    submissionResult: SubmissionResult,
    exercise: Exercise,
    project: Project
) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.FeedbackAvailableNotification.title"),
    MyBundle.message(
        "notification.FeedbackAvailableNotification.content",
        exercise.name, SubmissionResultUtil.getStatus(submissionResult)
    ),
    NotificationType.INFORMATION
) {
    /**
     * Construct a notification that notifies the user that feedback is available for a submission.
     * The notification contains a link that can be used to open the feedback and the amount of
     * points the submission got.
     */
    init {
        if (CourseManager.getInstance(project).state.feedbackCss != null) {
            super.addAction(ShowFeedbackNotificationAction(submissionResult, exercise))
        }
        super.addAction(OpenSubmissionNotificationAction(submissionResult))
    }
}
