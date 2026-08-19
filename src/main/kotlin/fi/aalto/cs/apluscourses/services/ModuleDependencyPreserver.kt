package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ModuleOrderEntry
import com.intellij.util.application
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps a removed A+ module in its dependants' dependency lists.
 *
 * Removing a module normally strips it from every other module that depends on it, and re-adding
 * the module later does not restore those entries. Instead, the dependency is re-added as an
 * *invalid* module entry, which resolves again as soon as the module is re-downloaded.
 */
internal class ModuleDependencyPreserver : ModuleListener {
    /** Names of the modules that depended on a module about to be removed, keyed by its name. */
    private val dependantsOfRemoved = ConcurrentHashMap<String, List<String>>()

    override fun beforeModuleRemoved(project: Project, module: Module) {
        if (CourseManager.course(project) == null) return

        val dependants = ModuleManager.getInstance(project).modules
            .filter { candidate ->
                candidate.name != module.name &&
                        candidate.rootManager.moduleDependencies.any { it.name == module.name }
            }
            .map { it.name }

        if (dependants.isNotEmpty()) {
            dependantsOfRemoved[module.name] = dependants
        }
    }

    override fun moduleRemoved(project: Project, module: Module) {
        val dependants = dependantsOfRemoved.remove(module.name) ?: return
        val moduleManager = ModuleManager.getInstance(project)

        dependants.forEach { dependantName ->
            val dependant = moduleManager.findModuleByName(dependantName) ?: return@forEach
            application.runWriteAction {
                val model = dependant.rootManager.modifiableModel
                val alreadyPresent = model.orderEntries
                    .filterIsInstance<ModuleOrderEntry>()
                    .any { it.moduleName == module.name }
                if (alreadyPresent) {
                    model.dispose()
                } else {
                    model.addInvalidModuleEntry(module.name)
                    model.commit()
                }
            }
        }
        CoursesLogger.info(
            "Kept dependency on removed module ${module.name} in ${dependants.joinToString(", ")}"
        )
    }
}
