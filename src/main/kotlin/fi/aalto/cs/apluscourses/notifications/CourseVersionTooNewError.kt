package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class CourseVersionTooNewError
/**
 * Notification to be shown when the plugin is too new to support the current course.
 */
    : Notification(
    PluginSettings.A_PLUS, MyBundle.message("notification.CourseVersionError.title"),
    MyBundle.message("notification.CourseVersionTooNewError.content"), NotificationType.ERROR
)
