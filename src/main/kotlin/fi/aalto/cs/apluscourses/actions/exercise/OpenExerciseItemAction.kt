package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.application
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.services.exercise.SelectedExerciseService
import fi.aalto.cs.apluscourses.ui.browser.BrowserEditor
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView
import icons.PluginIcons
import kotlinx.coroutines.launch


class OpenExerciseItemAction : AnAction() {
    private var loading = false
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedItem = project.service<SelectedExerciseService>().selectedExerciseTreeItem
        val url = selectedItem?.url() ?: return
        if (selectedItem is ExercisesView.SubmissionResultItem) {
            APlusApi.cs().launch {
                // The URL of a submission result has to first be fetched from the API
                loading = true
                val htmlUrl = selectedItem.submission.getHtmlUrl()
                loading = false
                browse(htmlUrl, project)
            }
        } else {
            browse(url, project)
        }
    }

    private fun browse(url: String, project: Project) {
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
//        FileEditorManager.getInstance(project).openEditor(editor, true)
//        JBCefBrowser.create(project).loadURL(url)
//        BrowserUtil.browse(url, project)
    }

    override fun update(e: AnActionEvent) {
        val selected = e.project?.service<SelectedExerciseService>()?.selectedExerciseTreeItem
        e.presentation.isEnabled = selected?.url() != null
        e.presentation.icon = if (loading) PluginIcons.A_PLUS_LOADING else PluginIcons.A_PLUS_BROWSE
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}