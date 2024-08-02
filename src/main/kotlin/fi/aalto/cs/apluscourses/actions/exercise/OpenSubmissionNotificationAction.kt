package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.Opener

class OpenSubmissionNotificationAction(
    private val submissionResult: SubmissionResult
) : NotificationAction(MyBundle.message("notification.OpenSubmissionNotificationAction.content")) {


    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        e.project?.service<Opener>()?.openSubmission(submissionResult)
    }
}
