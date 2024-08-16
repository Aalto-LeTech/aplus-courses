package fi.aalto.cs.apluscourses.services

import com.intellij.ide.browsers.actions.OpenInBrowserBaseGroupAction.OpenInBrowserEditorContextBarGroupAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.InspectionWidgetActionProvider
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.FileUtil
import fi.aalto.cs.apluscourses.icons.CoursesIcons

internal class OpenDocumentationActionProvider : InspectionWidgetActionProvider {
    override fun createAction(editor: Editor): AnAction? {
        val project = editor.project ?: return null
        val course = CourseManager.course(project) ?: return null
        val virtualFile = editor.virtualFile ?: return null
        if (virtualFile.extension != "scala") return null
        val fileName = virtualFile.nameWithoutExtension
        val path = virtualFile.path
        println(path)
        println(path.replace("/o1/", "/doc/o1/").substringBeforeLast("/"))

//        val module =
//            ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(virtualFile) ?: return null
        val file = FileUtil.findFileInDirectoryStartingWith(
            path.replace("/o1/", "/doc/o1/").substringBeforeLast("/"), fileName
        ) ?: return null
        println(
            file
        )
        val action = object : DumbAwareAction("Show Documentation") {
            override fun actionPerformed(e: AnActionEvent) {
                val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(file) ?: return
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
                val newDataContext = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project)
                    .add(CommonDataKeys.PSI_FILE, psiFile).build()

                OpenInBrowserEditorContextBarGroupAction().getChildren(e)[0].actionPerformed(
                    e.withDataContext(newDataContext)
                )
//                    AnActionEvent(
//                        e.inputEvent,
//                        newDataContext,
//                        e.place,
//                        e.presentation,
//                        e.actionManager,
//                        e.modifiers
//                )
            }

            override fun update(e: AnActionEvent) {
                e.presentation.icon = CoursesIcons.Docs
            }

            override fun getActionUpdateThread(): ActionUpdateThread {
                return ActionUpdateThread.BGT
            }

        }
        return action
    }
}