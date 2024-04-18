package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService


class RefreshExercisesAction : DumbAwareAction() {

    override fun update(e: AnActionEvent) {
//        if (e.isFromActionToolbar) {
//            e.presentation.text = message("intellij.actions.RefreshExerciseAction.tooltip")
//        }
        e.presentation.setEnabled(
            e.project != null// && courseProject != null && courseProject.getAuthentication() != null
        )
    }


    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<ExercisesUpdaterService>()?.restart()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

}
