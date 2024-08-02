package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class CourseConfigListErrorNotification
/**
 * Constructs a notification that notifies the user of an error about course configs.
 */
    : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.CourseConfigListErrorNotification.title"),
    MyBundle.message("notification.CourseConfigListErrorNotification.content"),
    NotificationType.ERROR
)

