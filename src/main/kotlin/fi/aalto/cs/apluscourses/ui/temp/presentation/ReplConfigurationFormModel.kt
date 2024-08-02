package fi.aalto.cs.apluscourses.ui.temp.presentation

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import java.util.Arrays
import java.util.stream.Collectors

/**
 * Creates a model for [ReplConfigurationForm].
 *
 * @param project                a [Project] to extract [Module]s from
 * @param moduleWorkingDirectory a path to the workDir of the [Module] in focus
 * @param targetModuleName       a [String] name of the [Module] in focus
 */

class ReplConfigurationFormModel(
    val project: Project,
    var moduleWorkingDirectory: String,
    var targetModuleName: String
) {
    val moduleNames: List<String> = getScalaModuleNames(getModules(project))
    var startRepl = true


    /**
     * Method additionally to setting a [Project] updates the list of affiliated [Module]
     * names.
     *
     * @param project a [Project] to set and extract [Module]s to update the list of
     * names.
     */
//    fun setProject(project: Project) {
//        this.project = project
//        val modules = getModules(project)
//        this.moduleNames = getScalaModuleNames(modules)
//    }

    /**
     * Method to extract [Module]s from [Project].
     *
     * @param project a [Project] to extract [Module]s from
     * @return an array of [Module]
     */
    private fun getModules(project: Project): Array<Module> {
        return ModuleManager.getInstance(project).modules
    }

    companion object {
        /**
         * Filters out the names of Scala modules for the [Module] array.
         *
         * @param modules [Module] array to process
         * @return a [List] of [String] for names of the modules
         */
        fun getScalaModuleNames(modules: Array<Module>): List<String> {
            return modules
                .filter { module: Module ->
                    // Java and Scala modules used to be called "JAVA_MODULE".
                    // Then, it was changes to "Java Module". We will check for both.
                    val moduleName = ModuleType.get(module!!).name
                    moduleName.equals("JAVA_MODULE", ignoreCase = true) || moduleName.equals(
                        "Java Module",
                        ignoreCase = true
                    )
                }
                .map { it.name }
        }
    }
}
