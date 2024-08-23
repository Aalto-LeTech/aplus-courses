package fi.aalto.cs.apluscourses.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.LibraryTable
import fi.aalto.cs.apluscourses.model.component.*
import fi.aalto.cs.apluscourses.utils.Version
import fi.aalto.cs.apluscourses.utils.callbacks.Callbacks
import io.ktor.http.*

/**
 * @param oldModules List of all modules in this course. If the course object is created with [Course.fromConfigurationData], then the modules are returned in the order in which they are listed in the course configuration data.
 * @param oldLibraries Libraries (not including common libraries) of the course.
 * @param exerciseModules Mapping of exercise IDs to modules. The keys are exercise IDs, and the values are maps from language codes to module names. Note that some exercises use modules that are not in the course configuration file, so the modules may not be in [Course.getModules].
 * @param resourceUrls URLs of resources related to the course. The keys are the names of the resources and the values are the URLs.
 */
data class Course(
    val id: Long,
    val name: String,
    val htmlUrl: String,
    val imageUrl: String,
    val endingTime: String,
    val languages: List<String>,
    val modules: List<Module>,
    val exerciseModules: Map<Long, Map<String, Module>>,
    val resourceUrls: Map<String, Url>,
    val optionalCategories: List<String>,
    val autoInstallComponentNames: List<String>,
    val replInitialCommands: Map<String, List<String>>?,
    val replAdditionalArguments: String?,
    val minimumPluginVersion: Version,
    val hiddenElements: List<Long>,
    val callbacks: Callbacks,
//    val feedbackParser: String?,
    private val project: Project,
) {
    val commonLibraries: MutableList<Library> = mutableListOf()
    val components: Map<String, Component<*>>
        get() = (modules + commonLibraries).associateBy { it.name }

    /**
     * A list of components that should be installed automatically for this course.
     */
    val autoInstallComponents: List<Component<*>> =
        autoInstallComponentNames
            .mapNotNull { components[it] }

    /**
     * A list of modules that have an update available.
     */
    fun updatableModules(): List<Module> =
        modules.filter { it.isUpdateAvailable }

    fun getComponentIfExists(name: String): Component<*>? {
        val component = components[name]
        return component ?: createAndCacheLibrary(name)
    }

    private fun createAndCacheLibrary(name: String): Library? {
        println("createAndCacheLibrary $name")
        if (name.contains("scala-sdk")) {
            try {
                val library = ScalaSdk(name, project)
                commonLibraries.add(library)
                return library
            } catch (e: IllegalArgumentException) {
                return null
            }
        }
        return null
    }

    init {
        val libraryTableListener: LibraryTable.Listener = object : LibraryTable.Listener {
            override fun afterLibraryRemoved(
                library: com.intellij.openapi.roots.libraries.Library
            ) {
                library.name?.let { name -> getComponentIfExists(name)?.updateStatus() }
            }
        }
        Library.libraryTable(project).addListener(libraryTableListener)
    }
}
