package fi.aalto.cs.apluscourses.services

import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class Notifier(
    val project: Project,
    val cs: CoroutineScope
) {
    private fun notify(notification: Notification) {
        Notifications.Bus.notify(notification, project)
    }

    private fun notifyAndHide(notification: Notification, timeoutMs: Long) {
        // IntelliJ implementation of notifyAndHide is missing the project parameter from notify
        notify(notification)

        cs.launch {
            try {
                delay(timeoutMs)
                notification.hideBalloon()
            } catch (e: CancellationException) {
                CoursesLogger.warn("Notification timeout interrupted")
                throw e
            }
        }
    }

    companion object {
        private fun getInstance(project: Project): Notifier {
            return project.service<Notifier>()
        }

        fun notify(notification: Notification, project: Project) {
            getInstance(project).notify(notification)
        }

        /**
         * Shows a notification and hides it after the specified time.
         *
         * @param notification The Notification to be shown.
         * @param project      The Project where the Notification is shown.
         * @param timeoutMs    The time in milliseconds after which the notification is hidden.
         */
        fun notifyAndHide(notification: Notification, project: Project, timeoutMs: Long = 10000L) {
            getInstance(project).notifyAndHide(notification, timeoutMs)
        }
    }
}
