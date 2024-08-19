package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import fi.aalto.cs.apluscourses.services.exercise.SelectedExercise
import fi.aalto.cs.apluscourses.services.exercise.SubmitExercise
import fi.aalto.cs.apluscourses.utils.APlusLogger

class SubmitExerciseAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val selectedExercise = e.project?.service<SelectedExercise>()?.selectedExercise
        e.presentation.isEnabled = selectedExercise != null && selectedExercise.isSubmittable
    }

    override fun actionPerformed(e: AnActionEvent) {
        APlusLogger.logger.debug("Starting SubmitExerciseAction")
        val project = e.project ?: return
        val exercise = project.service<SelectedExercise>().selectedExercise ?: return
        project.service<SubmitExercise>().submit(exercise)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
