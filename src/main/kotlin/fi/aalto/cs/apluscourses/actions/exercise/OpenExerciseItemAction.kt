package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.exercise.SelectedExercise
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView


class OpenExerciseItemAction : AnAction() {
    private var loading = false
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedItem = project.service<SelectedExercise>().selectedExerciseTreeItem
        val url = selectedItem?.url() ?: return
        if (selectedItem is ExercisesView.SubmissionResultItem) {
            loading = true
            project.service<Opener>().openSubmissionResult(selectedItem.submission) {
                loading = false
            }
        } else {
            browse(url, project)
        }
    }

    private fun browse(url: String, project: Project) {
        /* Embedded browser for the future
        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.allEditors.forEach { editor: FileEditor ->
            if (editor.file != null && editor.file.extension == "aplus") {
                (editor as BrowserEditor).loadURL(url)
                return
            }
        }
        val file = LightVirtualFile("browse.aplus", "url:$url")
        application.invokeLater {
            fileEditorManager.openFile(file, true)
        }
         */
        BrowserUtil.browse(url, project)
    }

    override fun update(e: AnActionEvent) {
        val selected = e.project?.service<SelectedExercise>()?.selectedExerciseTreeItem
        e.presentation.isEnabled = selected?.url() != null
        e.presentation.icon = if (loading) CoursesIcons.Loading else CoursesIcons.Browse
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}