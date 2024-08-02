package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

object ProjectModuleSource {
    fun getModules(project: Project): Array<Module> {
        return ModuleManager.getInstance(project).modules
    }

    fun getModule(project: Project, moduleName: String): Module? {
        return ModuleManager.getInstance(project).findModuleByName(moduleName)
    }
}
