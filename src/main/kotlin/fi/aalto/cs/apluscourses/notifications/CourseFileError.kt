package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class CourseFileError
/**
 * Error to be shown when course settings file cannot be accessed.
 *
 * @param e Exception.
 */
    (e: Exception) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.CourseFileError.title"),
    MyBundle.message("notification.CourseFileError.content", e.message!!),
    NotificationType.ERROR
)
