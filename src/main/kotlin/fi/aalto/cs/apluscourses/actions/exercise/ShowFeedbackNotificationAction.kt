package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.services.exercise.ShowFeedback

class ShowFeedbackNotificationAction(
    private val submissionResult: SubmissionResult,
    private val exercise: Exercise
) :
    NotificationAction(MyBundle.message("notification.ShowFeedbackNotificationAction.content")) {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        e.project?.service<ShowFeedback>()?.showFeedback(submissionResult, exercise)
    }
}
