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
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.FileUtil

internal class OpenDocumentationActionProvider : InspectionWidgetActionProvider {
    override fun createAction(editor: Editor): AnAction? {
        val project = editor.project ?: return null
        val course = CourseManager.course(project) ?: return null
        if (!course.name.contains("O1")) return null
        val virtualFile = editor.virtualFile ?: return null
        if (virtualFile.extension != "scala") return null
        val fileName = virtualFile.nameWithoutExtension
        val path = virtualFile.path

        val file = FileUtil.findFileInDirectoryStartingWith(
            // TODO: O1 specific
            path.replace("/o1/", "/doc/o1/").substringBeforeLast("/"), fileName
        ) ?: return null

        val action = object : DumbAwareAction("Show Documentation") {
            override fun actionPerformed(e: AnActionEvent) {
                val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(file) ?: return
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
                val newDataContext = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project)
                    .add(CommonDataKeys.PSI_FILE, psiFile).build()
                OpenInBrowserEditorContextBarGroupAction().getChildren(e)[0].actionPerformed(
                    e.withDataContext(newDataContext)
                )
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