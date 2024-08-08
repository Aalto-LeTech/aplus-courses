package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class NetworkErrorNotification
/**
 * Constructs a notification that notifies the user of an IO error arising from the HTTP client.
 *
 * @param exception An exception that caused this notification.
 */(val exception: Exception) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.NetworkErrorNotification.title"),
    MyBundle.message(
        "notification.NetworkErrorNotification.content",
        exception.message!!
    ),
    NotificationType.ERROR
)
