package fi.aalto.cs.apluscourses.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import fi.aalto.cs.apluscourses.services.course.CourseManager

class RefreshEverythingAction : DumbAwareAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<CourseManager>()?.restart()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

}