package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class SubmissionSentNotification
/**
 * Construct a notification which informs the user that an exercise was submitted successfully.
 */
    : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.SuccessfulSubmissionNotification.title"),
    MyBundle.message("notification.SuccessfulSubmissionNotification.content"),
    NotificationType.INFORMATION
)
