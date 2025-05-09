package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.UnloadedModuleDescription
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ui.configuration.actions.ModuleDeleteProvider
import com.intellij.util.application
import fi.aalto.cs.apluscourses.services.course.CourseManager
import org.jetbrains.annotations.ApiStatus

@Service(Service.Level.APP)
internal class APlusModuleDeleteProvider : ModuleDeleteProvider() {
    /**
     * When removing a module from an A+ Courses project,
     * it should not be deleted from other modules' dependencies
     * because it would not get re-added when the module is re-downloaded.
     */
    @ApiStatus.Experimental
    override fun doDetachModules(
        project: Project,
        modules: Array<Module>?,
        unloadedModules: List<UnloadedModuleDescription>?
    ) {
        // Only handle modules in A+ Courses projects
        CourseManager.course(project) ?: return super.doDetachModules(project, modules, unloadedModules)

        val moduleNamesToDelete = modules?.map { it.name } ?: emptyList()
        val dependants = ModuleManager
            .getInstance(project)
            .modules.associateWith { module ->
                module.rootManager.moduleDependencies.filter {
                    moduleNamesToDelete.contains(it.name)
                }
            }
            .filter { (_, dependencies) -> dependencies.isNotEmpty() }

        super.doDetachModules(project, modules, unloadedModules)

        // Re-add the module to its dependants
        dependants.forEach { (module, dependencies) ->
            val rootModel = module.rootManager.modifiableModel
            application.runWriteAction {
                dependencies.forEach { dependency ->
                    rootModel.addModuleOrderEntry(dependency)
                }
                rootModel.commit()
            }
        }
    }
}
