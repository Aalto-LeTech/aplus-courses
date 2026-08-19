package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings
import org.jetbrains.annotations.NonNls

/**
 * Tells the user that plugins the course requires are installed but disabled.
 */
class DisabledPluginsNotification(
    pluginNames: Collection<String>,
    project: Project
) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.DisabledPluginsNotification.title"),
    MyBundle.message(
        "notification.DisabledPluginsNotification.content",
        pluginNames.joinToString(", ")
    ),
    NotificationType.WARNING
) {
    init {
        addAction(
            NotificationAction.createSimple(
                MyBundle.message("notification.DisabledPluginsNotification.action")
            ) {
                @NonNls val pluginsPage = "Plugins"
                ShowSettingsUtil.getInstance().showSettingsDialog(project, pluginsPage)
            }
        )
    }
}
