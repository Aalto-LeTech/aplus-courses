package fi.aalto.cs.apluscourses.services

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.browsers.actions.OpenInBrowserBaseGroupAction.OpenInBrowserEditorContextBarGroupAction
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiManager
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.icons.CoursesIcons
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

    fun openDocumentation(module: Module) {
        cs.launch {
            val dir = module.platformObject?.guessModuleDir() ?: return@launch
            val virtualFile = dir.findFile("doc/index.html") ?: return@launch
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return@launch
            val newDataContext = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.PSI_FILE, psiFile).build()
            val actionEvent = AnActionEvent.createFromDataContext(ActionPlaces.TOOLWINDOW_CONTENT, null, newDataContext)
            val action = OpenInBrowserEditorContextBarGroupAction().getChildren(null)[0]
            withContext(Dispatchers.EDT) {
                action.actionPerformed(actionEvent)
            }
        }
    }

    fun showModuleInProjectTreeAction(module: Module): AnAction {
        return object : DumbAwareAction("Show in Project Tree") {
            override fun actionPerformed(e: AnActionEvent) {
                showModuleInProjectTree(module)
            }

            override fun update(e: AnActionEvent) {
                e.presentation.text = "Show in Project Tree"
                e.presentation.icon = AllIcons.General.Locate
            }

            override fun getActionUpdateThread(): ActionUpdateThread {
                return ActionUpdateThread.EDT
            }
        }
    }

    fun openDocumentationAction(module: Module, fileName: String): AnAction {
        return object : DumbAwareAction("Show Documentation") {
            override fun actionPerformed(e: AnActionEvent) {
                val dir = module.platformObject?.guessModuleDir() ?: return
                println(dir)
//        val file = FileUtil.findFileInDirectoryStartingWith(
//            path.replace("/o1/", "/doc/o1/").substringBeforeLast("/"), fileName
//        val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(file) ?: return
                val virtualFile = dir.findFile(fileName) ?: return
                println(virtualFile)
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
                return ActionUpdateThread.EDT
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
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
                .syncPublisher(SHOW_ITEM_TOPIC)
                .onExerciseOpened(exercise)
        }
    }

    private fun fireOpenModule(module: Module) {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
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