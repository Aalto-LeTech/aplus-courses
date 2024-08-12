package fi.aalto.cs.apluscourses.ui.module

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.ui.FileRenderer
import fi.aalto.cs.apluscourses.ui.FileTree
import java.nio.file.Path
import kotlin.io.path.name

class UpdateModuleDialog(val project: Project, val module: Module, val files: List<Path>) :
    DialogWrapper(project) {
    init {
        setOKButtonText("Update")
        title = "Update ${module.name}"
        init()
    }

    override fun createCenterPanel(): DialogPanel = panel {
        row {
            label("Modified files:")
        }
        row {
            cell(FileTree(files, project)).applyToComponent {
                cellRenderer = FileRenderer(files.associate { it.name to it })
                isEnabled = false
            }
        }
        row {
            text("You are about to update ${module.name} to version ${module.latestVersion}. The files you have modified will be moved to the <code>backup</code> folder. Continue updating?")
        }
    }
}