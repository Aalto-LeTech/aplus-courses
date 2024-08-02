package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings
import java.nio.file.Path

/**
 * Construct a missing file notification that explains that a file with the given name couldn't be
 * found in the given module.
 */
class MissingFileNotification
/**
 * Construct a missing file notification that explains that a file with the given name couldn't be
 * found in the given module.
 */ @JvmOverloads constructor(val path: Path, val filename: String, download: Boolean = false) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.MissingFileNotification.title"),
    MyBundle.message(
        if (download)
            "notification.MissingFileNotification.contentDownload"
        else
            "notification.MissingFileNotification.content", filename, path
    ),
    NotificationType.ERROR
)
