package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class NotSubmittableNotification
/**
 * Construct a notification that explains that the exercise cannot be submitted from the plugin.
 */
    : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.NotSubmittableNotification.title"),
    MyBundle.message("notification.NotSubmittableNotification.content"),
    NotificationType.ERROR
)
