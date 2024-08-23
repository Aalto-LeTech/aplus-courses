package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.services.PluginSettings
import java.nio.file.Path

class ModuleUpdatedNotification(module: Module, addedFiles: List<Path>, removedFiles: List<Path>) : Notification(
    PluginSettings.A_PLUS,
    message("notification.ModuleUpdatedNotification.title"),
    notificationContent(module, addedFiles, removedFiles),
    NotificationType.INFORMATION
) {
    companion object {
        private fun pathsToHtmlList(paths: List<Path>, module: Module): String {
            fun relativePath(path: Path): String {
                return module.fullPath.relativize(path).toString()
            }
            return paths.filter {
                it.toString().contains('.')
            } // Filter out directories, while keeping removed files
                .joinToString(separator = "") { "<li>${relativePath(it)}</li>" }
        }

        private fun notificationContent(module: Module, addedFiles: List<Path>, removedFiles: List<Path>): String {
            val baseText = message("notification.ModuleUpdatedNotification.content", module.name, module.latestVersion)
            val addedFilesText =
                if (addedFiles.isNotEmpty()) message(
                    "notification.ModuleUpdatedNotification.addedFiles",
                    pathsToHtmlList(addedFiles, module)
                ) else null
            val removedFilesText =
                if (removedFiles.isNotEmpty()) message(
                    "notification.ModuleUpdatedNotification.removedFiles",
                    pathsToHtmlList(removedFiles, module)
                ) else null
            return listOfNotNull(baseText, addedFilesText, removedFilesText).joinToString(separator = "<br>")
        }
    }
}