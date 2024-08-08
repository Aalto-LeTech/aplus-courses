package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class ApiTokenNotSetNotification
/**
 * Constructs a notification that tells authentication is not set.
 */
    : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.ApiTokenNotSet.title"),
    MyBundle.message("notification.ApiTokenNotSet.content"),
    NotificationType.INFORMATION
)
