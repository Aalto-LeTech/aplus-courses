package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings
import java.io.IOException

class IoErrorNotification
/**
 * Construct an error notification that tells the user of an IO error.
 *
 * @param exception The exception corresponding to the IO error.
 */(val exception: IOException) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.IoErrorNotification.title"),
    MyBundle.message(
        "notification.IoErrorNotification.content",
        exception.message!!
    ),
    NotificationType.ERROR
)
