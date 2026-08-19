package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

/**
 * Explains that a module declares dependencies on modules the course does not provide, so they
 * cannot be installed automatically.
 *
 * Raised by CourseManager when refreshing module statuses.
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
