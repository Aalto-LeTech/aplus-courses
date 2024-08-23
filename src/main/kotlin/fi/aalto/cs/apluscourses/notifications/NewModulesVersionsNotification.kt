package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.services.PluginSettings

/**
 * A [Notification] wrapper to let the user know about some A+ Course modules having the new
 * versions of them in A+ LMS.
 */
class NewModulesVersionsNotification(modules: List<Module>) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message(
        if (modules.size == 1)
            "notification.NewModulesVersionsNotification.titleSingle"
        else
            "notification.NewModulesVersionsNotification.title"
    ),
    MyBundle.message(
        if (modules.size == 1)
            "notification.NewModulesVersionsNotification.contentSingle"
        else
            "notification.NewModulesVersionsNotification.content",
        modules.map { it.name }.joinToString(", ")
    ),
    NotificationType.INFORMATION
)