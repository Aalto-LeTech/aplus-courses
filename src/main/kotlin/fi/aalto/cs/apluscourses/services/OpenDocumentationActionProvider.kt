package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.InspectionWidgetActionProvider
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFileManager
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.FileUtil
import org.jetbrains.annotations.NonNls

internal class OpenDocumentationActionProvider : InspectionWidgetActionProvider {
    override fun createAction(editor: Editor): AnAction? {
        val project = editor.project ?: return null
        val course = CourseManager.course(project) ?: return null
        @NonNls val o1 = "O1" // TODO: O1 specific
        if (!course.name.contains(o1)) return null
        val virtualFile = editor.virtualFile ?: return null
        if (virtualFile.extension != "scala") return null
        val fileName = virtualFile.nameWithoutExtension
        val path = virtualFile.path

        val file = FileUtil.findFileInDirectoryStartingWith(
            // TODO: O1 specific
            path.replace("/o1/", "/doc/o1/").substringBeforeLast("/"), fileName
        ) ?: return null

        val action = object : DumbAwareAction(message("services.Opener.showDocumentationAction")) {
            override fun actionPerformed(e: AnActionEvent) {
                val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(file) ?: return
                project.service<Opener>().openHtmlFileInEmbeddedBrowser(virtualFile)
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