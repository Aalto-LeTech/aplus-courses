package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class MissingModuleNotification
/**
 * Construct a missing module notification that explains that a module with the given name
 * couldn't be found.
 */(val moduleName: String) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.MissingModuleNotification.title"),
    MyBundle.message("notification.MissingModuleNotification.content", moduleName),
    NotificationType.ERROR
)
