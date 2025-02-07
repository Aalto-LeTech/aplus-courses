package fi.aalto.cs.apluscourses.ui.module

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.ui.FileRenderer
import fi.aalto.cs.apluscourses.ui.FileTree
import java.nio.file.Path
import kotlin.io.path.name

class UpdateModuleDialog(val project: Project, val module: Module, val files: List<Path>) :
    DialogWrapper(project) {
    init {
        setOKButtonText(message("ui.UpdateModuleDialog.okButton"))
        title = message("ui.UpdateModuleDialog.title", module.name)
        init()
    }

    override fun createCenterPanel(): DialogPanel = panel {
        row {
            label(message("ui.UpdateModuleDialog.modifiedFiles"))
        }
        row {
            cell(FileTree(files, project)).applyToComponent {
                cellRenderer = FileRenderer(files.associateBy { it.name })
                isEnabled = false
            }
        }
        row {
            text(message("ui.UpdateModuleDialog.description", module.name, module.latestVersion))
        }
    }
}