package fi.aalto.cs.apluscourses.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.wm.ToolWindowManager
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.Opener
import org.jetbrains.annotations.NonNls

class ShowNewsNotificationAction :
    NotificationAction(MyBundle.message("notification.ShowNewsNotificationAction.content")) {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        val project = e.project ?: return
        ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)?.activate {
            project.service<Opener>().showNews()
        }
    }
}

@NonNls
private const val TOOL_WINDOW_ID = "A+ Courses"
