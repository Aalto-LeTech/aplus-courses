package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

/**
 * A notification for a course configuration whose A+ address cannot be used as a URL.
 *
 * @param url The unusable address, as the course configuration gave it.
 */
class InvalidCourseUrlNotification(val url: String) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.InvalidCourseUrlNotification.title"),
    MyBundle.message("notification.InvalidCourseUrlNotification.content", url),
    NotificationType.ERROR
)
