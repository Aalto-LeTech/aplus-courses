package fi.aalto.cs.apluscourses.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import fi.aalto.cs.apluscourses.services.ModuleImportExport

class ExportModuleAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        project.service<ModuleImportExport>().exportModule()
    }
} 