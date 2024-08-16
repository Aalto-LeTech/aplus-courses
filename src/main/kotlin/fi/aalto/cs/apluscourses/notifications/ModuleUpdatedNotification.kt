package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.services.PluginSettings
import java.nio.file.Path

class ModuleUpdatedNotification(module: Module, addedFiles: List<Path>, removedFiles: List<Path>) : Notification(
    PluginSettings.A_PLUS,
    "Module updated",
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
            val baseText = "${module.name} has been updated to version ${module.latestVersion}.<vr>"
            val addedFilesText =
                if (addedFiles.isNotEmpty()) "Added files: <ul>${pathsToHtmlList(addedFiles, module)}</ul>" else null
            val removedFilesText =
                if (removedFiles.isNotEmpty())
                    "<br>Removed files: <ul>${pathsToHtmlList(removedFiles, module)}</ul>" else null
            return listOfNotNull(baseText, addedFilesText, removedFilesText).joinToString(separator = "<br>")
        }
    }
}