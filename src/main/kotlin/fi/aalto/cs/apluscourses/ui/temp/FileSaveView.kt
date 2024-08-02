package fi.aalto.cs.apluscourses.ui.temp

import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.ui.temp.presentation.FileSaveViewModel
import java.nio.file.InvalidPathException

class FileSaveView(
    viewModel: FileSaveViewModel,
    project: Project
) {
    private val viewModel: FileSaveViewModel

    private val project: Project

    init {
        this.viewModel = viewModel
        this.project = project
    }

    fun showAndGet(): Boolean {
        val descriptor = FileSaverDescriptor(viewModel.getTitle(), viewModel.getDescription())

        while (true) {
            val dialog = FileChooserFactory
                .getInstance()
                .createSaveFileDialog(descriptor, project)
            val file = dialog.save(
                viewModel.getDefaultDirectory(), viewModel.getDefaultName()
            )

            if (file != null) {
                try {
                    viewModel.setPath(file.file.toPath())
                    return true
                } catch (e: InvalidPathException) {
                    Messages.showErrorDialog(
                        message("ui.exportModule.invalidPath.message", e.reason),
                        message("ui.exportModule.invalidPath.title")
                    )
                    continue
                }
            }

            return false
        }
    }
}
