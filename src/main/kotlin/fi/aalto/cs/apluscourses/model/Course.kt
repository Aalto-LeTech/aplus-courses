package fi.aalto.cs.apluscourses.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.LibraryTable
import fi.aalto.cs.apluscourses.model.component.*
import fi.aalto.cs.apluscourses.utils.Version
import fi.aalto.cs.apluscourses.utils.callbacks.Callbacks
import io.ktor.http.*
import org.jetbrains.annotations.NonNls

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

    fun getComponentIfExists(name: String): Component<*>? {
        val component = components[name]
        return component ?: createAndCacheLibrary(name)
    }

    private fun createAndCacheLibrary(name: String): Library? {
        @NonNls val scalaSdkName = "scala-sdk"
        if (name.contains(scalaSdkName)) {
            try {
                val library = ScalaSdk(name, project)
                commonLibraries.add(library)
                return library
            } catch (_: IllegalArgumentException) {
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
