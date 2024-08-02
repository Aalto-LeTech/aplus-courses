package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class CourseVersionOutdatedError
/**
 * Notification to be shown when the plugin is outdated with respect to the current course.
 * This represents a mismatch in the major version and is an error.
 */
    : Notification(
    PluginSettings.A_PLUS, MyBundle.message("notification.CourseVersionError.title"),
    MyBundle.message("notification.CourseVersionOutdatedError.content"), NotificationType.ERROR
)
