package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

/**
 * A notification for an item A+ gave no page address for, so there is nothing to open.
 */
class UnavailableUrlNotification : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.UnavailableUrlNotification.title"),
    MyBundle.message("notification.UnavailableUrlNotification.content"),
    NotificationType.WARNING
)
