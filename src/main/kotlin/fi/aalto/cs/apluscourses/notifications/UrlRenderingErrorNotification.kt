package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class UrlRenderingErrorNotification
/**
 * Construct a notification informing the user that an error occurred while attempting to render
 * a submission.
 */(val exception: Exception) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.UrlRenderingErrorNotification.title"),
    MyBundle.message(
        "notification.UrlRenderingErrorNotification.content",
        exception.message!!
    ),
    NotificationType.ERROR
)
