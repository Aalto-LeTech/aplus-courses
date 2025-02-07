package fi.aalto.cs.apluscourses.notifications

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.Module
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.services.PluginSettings
import java.io.File

class ModuleExportedNotification(module: Module, exportedFile: File) : Notification(
    PluginSettings.A_PLUS,
    message("notification.ModuleExportedNotification.title"),
    message("notification.ModuleExportedNotification.content", module.name, exportedFile.path),
    NotificationType.INFORMATION
) {
    init {
        addAction(
            NotificationAction.createSimple(
                message("notification.ModuleExportedNotification.showInFiles")
            ) {
                RevealFileAction.openFile(exportedFile)
            })
    }
}