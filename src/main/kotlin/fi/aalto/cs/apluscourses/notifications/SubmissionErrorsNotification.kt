package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.util.text.StringUtil
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

/**
 * A notification that A+ created the submission but reported [errors] while grading it.
 */
class SubmissionErrorsNotification(errors: List<String>) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.SubmissionErrorsNotification.title"),
    MyBundle.message(
        "notification.SubmissionErrorsNotification.content",
        errors.joinToString("<br>") { StringUtil.escapeXmlEntities(it) }
    ),
    NotificationType.WARNING
)
