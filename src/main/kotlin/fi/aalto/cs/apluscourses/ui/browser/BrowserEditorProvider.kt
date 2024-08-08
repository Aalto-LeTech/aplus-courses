package fi.aalto.cs.apluscourses.ui.browser

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText

internal class BrowserEditorProvider : AsyncFileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean = file.extension == "aplus"
    override fun createEditor(project: Project, file: VirtualFile): FileEditor =
        createEditorAsync(project, file).build()

    override fun getEditorTypeId(): String = "aplus-jcef-editor"
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
    override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder =
        object : AsyncFileEditorProvider.Builder() {
            override fun build(): FileEditor {
//                if (NonProjectFileWritingAccessProvider.isWriteAccessAllowed(file, project)) {
//                    NonProjectFileWritingAccessProvider.allowWriting(listOf(file))
//                }
                val contents = file.readText()
                println("file contents: $contents")
                return if (contents.startsWith("url:")) {
                    BrowserEditor(project, file, contents.substringAfter("url:").trim())
                } else {
                    BrowserEditor(project, file, null)
                }
            }
        }
}