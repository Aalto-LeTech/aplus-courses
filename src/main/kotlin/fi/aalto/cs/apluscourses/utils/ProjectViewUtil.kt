package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.util.application

object ProjectViewUtil {
    /**
     * Method that adds a new file (pattern) to the list of files not being shown in the Project UI.
     *
     * @param ignoredFileName a [String] name of the file to be ignored.
     */
    fun ignoreFileInProjectView(
        ignoredFileName: String
    ) {
        val fileTypeManager = FileTypeManager.getInstance()

        if (!fileTypeManager.isFileIgnored(ignoredFileName)) {
            application.invokeLater {
                application.runWriteAction {
                    fileTypeManager.ignoredFilesList = "${fileTypeManager.ignoredFilesList};$ignoredFileName"
                }
            }
        }
    }
}
