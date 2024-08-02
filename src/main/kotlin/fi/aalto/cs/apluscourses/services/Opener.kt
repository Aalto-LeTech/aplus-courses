package fi.aalto.cs.apluscourses.services

import com.intellij.ide.browsers.actions.OpenInBrowserBaseGroupAction.OpenInBrowserEditorContextBarGroupAction
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.impl.ProjectViewImpl
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiManager
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.utils.FileUtil
import fi.aalto.cs.apluscourses.utils.ProjectViewUtil
import icons.PluginIcons
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class Opener(
    val project: Project,
    val cs: CoroutineScope
) {
    fun showModuleInProjectTree(module: Module) {
        val dir = module.platformObject?.guessModuleDir() ?: return
        println(dir)
        val psi = PsiManager.getInstance(project).findDirectory(dir) ?: return
        println(psi)
        (ProjectView.getInstance(project) as? ProjectViewImpl)?.select(psi, dir, true)
        println("selected")
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
                e.presentation.icon = PluginIcons.A_PLUS_DOCS
            }

            override fun getActionUpdateThread(): ActionUpdateThread {
                return ActionUpdateThread.BGT
            }

        }
    }

    fun showExercise(exercise: Exercise) {
        fireOpenExercise(exercise)
    }


    private fun fireOpenExercise(exercise: Exercise) {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
                .syncPublisher(OPEN_EXERCISE_TOPIC)
                .onExerciseOpened(exercise)
        }
    }

    interface ExerciseOpenerListener {
        @RequiresEdt
        fun onExerciseOpened(exercise: Exercise)
    }

    companion object {
        @ProjectLevel
        val OPEN_EXERCISE_TOPIC: Topic<ExerciseOpenerListener> =
            Topic(ExerciseOpenerListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)
    }
}