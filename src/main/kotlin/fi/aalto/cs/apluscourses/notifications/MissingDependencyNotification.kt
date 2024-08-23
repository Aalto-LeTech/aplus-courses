package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

/**
 * A missing module notification which explains that modules with the given names couldn't be found.
 */
class MissingDependencyNotification(moduleNames: Set<String>) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.MissingDependencyNotification.title"),
    MyBundle.message(
        "notification.MissingDependencyNotification.content",
        moduleNames.joinToString(", ")
    ),
    NotificationType.ERROR
)
// TODO show