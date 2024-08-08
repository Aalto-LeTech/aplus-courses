package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class MissingDependencyNotification
/**
 * Construct a missing module notification which explains that modules with the given names couldn't be found.
 */
    (moduleNames: Set<String?>) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.MissingDependencyNotification.title"),
    MyBundle.message(
        "notification.MissingDependencyNotification.content",
        java.lang.String.join(", ", moduleNames)
    ),
    NotificationType.ERROR
)
