package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import fi.aalto.cs.apluscourses.services.exercise.SelectedExerciseService
import fi.aalto.cs.apluscourses.services.exercise.ShowFeedback
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView.SubmissionResultItem

class ShowFeedbackAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selected = project.service<SelectedExerciseService>().selectedExerciseTreeItem
        if (selected is SubmissionResultItem) {
            project.service<ShowFeedback>().showFeedback(selected.submission, selected.exercise)
        }
    }

    override fun update(e: AnActionEvent) {
        val selected = e.project?.service<SelectedExerciseService>()?.selectedExerciseTreeItem
        e.presentation.isEnabled = selected != null && selected is SubmissionResultItem
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}