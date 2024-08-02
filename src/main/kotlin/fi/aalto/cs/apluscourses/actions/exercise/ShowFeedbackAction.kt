package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider.Companion.openEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.util.application
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.exercise.SelectedExerciseService
import fi.aalto.cs.apluscourses.services.exercise.ShowFeedback
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView.SubmissionResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.awt.Color
import java.io.IOException

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