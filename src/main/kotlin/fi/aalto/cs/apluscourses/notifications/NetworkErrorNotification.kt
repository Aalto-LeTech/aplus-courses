package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.aplus.client.AplusException

/**
 * A notification that notifies the user of an IO error arising from the HTTP client.
 *
 * @param exception An exception that caused this notification.
 */
class NetworkErrorNotification(val exception: Exception) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.NetworkErrorNotification.title"),
    MyBundle.message("notification.NetworkErrorNotification.content", userFacingMessage(exception)),
    NotificationType.ERROR
)

/**
 * Uses API status messages in notifications to avoid displaying endpoint URLs.
 * The full exception remains in the log for support.
 */
internal fun userFacingMessage(exception: Throwable): String = when (exception) {
    is AplusException.Unauthorized -> MyBundle.message("notification.apiError.unauthorized")
    is AplusException.Forbidden -> MyBundle.message("notification.apiError.forbidden")
    is AplusException.NotFound -> MyBundle.message("notification.apiError.notFound")
    // Pagination can fail after successful HTTP responses.
    is AplusException.Pagination -> MyBundle.message("notification.apiError.pagination")
    is AplusException -> MyBundle.message("notification.apiError.http", exception.status.value)
    else -> exception.message ?: exception.javaClass.simpleName
}
