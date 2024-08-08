package fi.aalto.cs.apluscourses.ui.browser

import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText

internal class APlusTabTitleProvider : EditorTabTitleProvider {
    override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
        if (file.extension == "aplus") {
            println("aplus file")
            return file.readText()
        }
        return null
    }
}