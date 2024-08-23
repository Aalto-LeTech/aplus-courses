package fi.aalto.cs.apluscourses.services

import com.intellij.ide.BrowserUtil
import com.intellij.ide.browsers.actions.OpenInBrowserBaseGroupAction.OpenInBrowserEditorContextBarGroupAction
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiManager
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun openSubmissionResult(submission: SubmissionResult, callback: () -> Unit) {
        cs.launch {
            val url = submission.getHtmlUrl(project)
            withContext(Dispatchers.EDT) {
                BrowserUtil.open(url)
                callback()
            }
        }
    }

    fun openDocumentationAction(module: Module, fileName: String): AnAction {
        return object : DumbAwareAction("Show Documentation") {
            override fun actionPerformed(e: AnActionEvent) {
                val dir = module.platformObject?.guessModuleDir() ?: return
                val virtualFile = dir.findFile(fileName) ?: return
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
                val newDataContext = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project)
                    .add(CommonDataKeys.PSI_FILE, psiFile).build()
                OpenInBrowserEditorContextBarGroupAction().getChildren(e)[0].actionPerformed(
                    e.withDataContext(newDataContext)
                )
            }

            override fun update(e: AnActionEvent) {
                e.presentation.text = "Show Documentation"
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

    fun openSubmission(submission: SubmissionResult) {
        cs.launch {
            BrowserUtil.open(submission.getHtmlUrl(project))
        }
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