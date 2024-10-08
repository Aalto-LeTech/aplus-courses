package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater


class RefreshExercisesAction : DumbAwareAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
        e.presentation.icon =
            if (e.project?.service<ExercisesUpdater>()?.isRunning == true) CoursesIcons.Loading
            else CoursesIcons.Refresh
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<ExercisesUpdater>()?.restart()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

}
