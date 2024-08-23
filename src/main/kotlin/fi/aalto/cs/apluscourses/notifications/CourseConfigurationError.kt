package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

/**
 * A notification that notifies the user of an error that occurred while attempting to
 * parse a course configuration file.
 *
 * @param exception An exception that caused this notification.
 */
class CourseConfigurationError(val exception: Exception) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.CourseConfigurationError.title"),
    MyBundle.message(
        "notification.CourseConfigurationError.content",
        exception.message!!
    ),
    NotificationType.ERROR
)
