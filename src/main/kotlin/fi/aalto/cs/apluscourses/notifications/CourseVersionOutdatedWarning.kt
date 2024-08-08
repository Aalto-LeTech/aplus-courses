package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class CourseVersionOutdatedWarning
/**
 * Notification to be shown when the plugin is outdated with respect to the current course.
 * This represents a mismatch in the minor version and is a warning.
 */
    : Notification(
    PluginSettings.A_PLUS, MyBundle.message("notification.CourseVersionError.title"),
    MyBundle.message("notification.CourseVersionOutdatedWarning.content"), NotificationType.WARNING
)
