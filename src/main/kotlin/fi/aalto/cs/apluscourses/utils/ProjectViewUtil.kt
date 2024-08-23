package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project

object ProjectViewUtil {
    /**
     * Method that adds a new file (pattern) to the list of files not being shown in the Project UI.
     *
     * @param ignoredFileName a [String] name of the file to be ignored.
     * @param project         a [Project] to ignore the file from.
     */
    fun ignoreFileInProjectView(
        ignoredFileName: String,
        project: Project
    ) {
        val fileTypeManager = FileTypeManager.getInstance()

        if (!fileTypeManager.isFileIgnored(ignoredFileName)) {
            WriteCommandAction.runWriteCommandAction(project) {
                fileTypeManager.ignoredFilesList = "${fileTypeManager.ignoredFilesList};$ignoredFileName"
            }
        }
    }
}
