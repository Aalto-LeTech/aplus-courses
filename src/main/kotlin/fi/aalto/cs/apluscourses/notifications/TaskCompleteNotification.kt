package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class TaskCompleteNotification : Notification {
    /**
     * Constructor.
     */
    private constructor(contentKey: String, index: Int) : super(
        PluginSettings.A_PLUS,
        MyBundle.message("notification.TaskCompleteNotification.title"),
        MyBundle.message(contentKey, index),
        NotificationType.INFORMATION
    )

    /**
     * Constructor.
     */
    private constructor(contentKey: String, index: Int, instructions: String) : super(
        PluginSettings.A_PLUS,
        MyBundle.message("notification.TaskCompleteNotification.title"),
        """
             ${MyBundle.message(contentKey, index)}
             ($instructions)
             """.trimIndent(),
        NotificationType.INFORMATION
    )

    companion object {
        fun createTaskCompleteNotification(index: Int): TaskCompleteNotification {
            return TaskCompleteNotification("notification.TaskCompleteNotification.content", index + 1)
        }

        fun createTaskAlreadyCompleteNotification(index: Int, instructions: String): TaskCompleteNotification {
            return TaskCompleteNotification(
                "notification.TaskCompleteNotification.contentAlready",
                index + 1, instructions
            )
        }
    }
}
