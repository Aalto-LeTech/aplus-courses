package fi.aalto.cs.apluscourses.services

import com.intellij.ide.BrowserUtil
import com.intellij.ide.browsers.WebBrowserService
import com.intellij.ide.browsers.actions.WebPreviewVirtualFile
import com.intellij.ide.browsers.createOpenInBrowserRequest
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorOpenRequest
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiManager
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.notifications.UnavailableUrlNotification
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls

@Service(Service.Level.PROJECT)
class Opener(
    val project: Project,
    val cs: CoroutineScope
) {
    fun showModuleInProjectTree(module: Module) {
        cs.launch {
            val dir = module.platformObject?.guessModuleDir() ?: return@launch
            val psi = readAction { PsiManager.getInstance(project).findDirectory(dir) } ?: return@launch
            withContext(Dispatchers.EDT) {
                ProjectView.getInstance(project).select(psi, dir, true)
                // TODO open project view
            }
        }
    }

    /**
     * Opens the submission page and always invokes [callback] on the EDT.
     * Callers use the callback to clear loading indicators after failure or cancellation.
     */
    fun openSubmissionResult(submission: SubmissionResult, callback: () -> Unit) {
        cs.launch {
            try {
                openSubmissionInBrowser(submission)
            } finally {
                withContext(NonCancellable + Dispatchers.EDT) { callback() }
            }
        }
    }

    fun openSubmission(submission: SubmissionResult) {
        cs.launch { openSubmissionInBrowser(submission) }
    }

    private suspend fun openSubmissionInBrowser(submission: SubmissionResult) {
        val url = submissionHtmlUrl(submission)
        if (url == null) {
            Notifier.notify(UnavailableUrlNotification(), project)
            return
        }
        withContext(Dispatchers.EDT) { BrowserUtil.open(url) }
    }

    private suspend fun submissionHtmlUrl(submission: SubmissionResult): String? {
        val htmlUrl = CoursesClient.getInstance(project).aplus.submission(submission.id).htmlUrl
        if (htmlUrl == null) CoursesLogger.warn("A+ gave no page URL for submission ${submission.id}")
        return htmlUrl
    }

    fun openHtmlFileInEmbeddedBrowser(htmlFile: VirtualFile) {
        val psiFile = PsiManager.getInstance(project).findFile(htmlFile) ?: return
        val request = createOpenInBrowserRequest(psiFile, true) ?: return

        val preferLocalUrl = false // Local urls break interactivity
        val url = WebBrowserService
            .getInstance()
            .getUrlsToOpen(request, preferLocalUrl)
            .firstOrNull() ?: return
        val webPreview = WebPreviewVirtualFile(
            htmlFile,
            url
        )
        FileEditorManager.getInstance(project)
            .requestOpenFile(webPreview, FileEditorOpenRequest.withFocus(true).withReuseOpen(true))
    }

    fun openDocumentationAction(module: Module, @NonNls fileName: String): AnAction {
        return object : DumbAwareAction(message("services.Opener.showDocumentationAction")) {
            override fun actionPerformed(e: AnActionEvent) {
                val dir = module.platformObject?.guessModuleDir() ?: return
                val virtualFile = dir.findFile(fileName) ?: return
                openHtmlFileInEmbeddedBrowser(virtualFile)
            }

            override fun update(e: AnActionEvent) {
                e.presentation.text = message("services.Opener.showDocumentationAction")
                e.presentation.icon = CoursesIcons.Docs
            }

            override fun getActionUpdateThread(): ActionUpdateThread {
                return ActionUpdateThread.BGT
            }

        }
    }

    fun showExercise(exercise: Exercise) {
        fireOpenExercise(exercise)
    }

    fun showModule(module: Module) {
        fireOpenModule(module)
    }

    private fun fireOpenExercise(exercise: Exercise) {
        application.invokeLater {
            project.messageBus
                .syncPublisher(SHOW_ITEM_TOPIC)
                .onExerciseOpened(exercise)
        }
    }

    private fun fireOpenModule(module: Module) {
        application.invokeLater {
            project.messageBus
                .syncPublisher(SHOW_ITEM_TOPIC)
                .onModuleOpened(module)
        }
    }

    interface ItemOpenerListener {
        @RequiresEdt
        fun onExerciseOpened(exercise: Exercise)

        @RequiresEdt
        fun onModuleOpened(module: Module)
    }

    companion object {
        @ProjectLevel
        val SHOW_ITEM_TOPIC: Topic<ItemOpenerListener> =
            Topic(ItemOpenerListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)
    }
}
