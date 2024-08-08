package fi.aalto.cs.apluscourses.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfoRt
import fi.aalto.cs.apluscourses.model.component.*
import fi.aalto.cs.apluscourses.utils.callbacks.Callbacks
import fi.aalto.cs.apluscourses.utils.temp.PluginDependency
import fi.aalto.cs.apluscourses.utils.Version
import io.ktor.http.*

/**
 * @param oldModules List of all modules in this course. If the course object is created with [Course.fromConfigurationData], then the modules are returned in the order in which they are listed in the course configuration data.
 * @param oldLibraries Libraries (not including common libraries) of the course.
 * @param exerciseModules Mapping of exercise IDs to modules. The keys are exercise IDs, and the values are maps from language codes to module names. Note that some exercises use modules that are not in the course configuration file, so the modules may not be in [Course.getModules].
 * @param resourceUrls URLs of resources related to the course. The keys are the names of the resources and the values are the URLs.
 */
class Course(
    val id: Long,
    val name: String,
    val aplusUrl: String,
    val htmlUrl: String,
    val imageUrl: String,
    val endingTime: String,
    val languages: List<String>,
    val modules: List<Module>,
//    val oldModules: List<OldModule>,
//    val oldLibraries: List<OldLibrary>,
    val exerciseModules: Map<Long, Map<String, Module>>,
    val resourceUrls: Map<String, Url>,
    val vmOptions: Map<String, String>,
    val optionalCategories: List<String>,
    val autoInstallComponentNames: List<String>,
    val replInitialCommands: Map<String, List<String>>?,
    val replAdditionalArguments: String?,
    val minimumPluginVersion: Version,
//    val commonLibraryProvider: CommonLibraryProvider,
//    val pluginDependencies: List<PluginDependency>, TODO
    val hiddenElements: List<Long>,
    val callbacks: Callbacks,
//    val requireAuthenticationForModules: Boolean,
//    val feedbackParser: String?,
//    val newsParser: String?,
//    val courseLastModified: Long, // TODO reimplement
    private val project: Project,
) {
    val commonLibraries: MutableList<Library> = mutableListOf()
    val components: Map<String, Component<*>>
        get() = (modules + commonLibraries).associateBy { it.name }
//        (oldModules + oldLibraries + commonLibraryProvider.providedLibraries).associateBy { it.originalName }


    /**
     * A list of components that should be installed automatically for this course.
     */
    val autoInstallComponents: List<Component<*>> =
        autoInstallComponentNames
            .mapNotNull { components[it] }

    /**
     * A URL containing the appropriate IDE settings for the platform that the user
     * is currently using. If no IDE settings are available, null is returned.
     */
    val appropriateIdeSettingsUrl: Url? =
        if (SystemInfoRt.isWindows) {
            resourceUrls["ideSettingsWindows"]
        } else if (SystemInfoRt.isLinux) {
            resourceUrls["ideSettingsLinux"]
        } else if (SystemInfoRt.isMac) {
            resourceUrls["ideSettingsMac"]
        } else {
            null
        } ?: resourceUrls["ideSettings"] // Use generic IDE settings if no platform-specific settings are available

//    /**
//     * Resolves states of unresolved components and calls `validate()`.
//     */
//    fun resolve() {
//        components.values.forEach { it.resolveState() }
//        validate()
//    }
//
//    /**
//     * Validates that components conform integrity constraints.
//     */
//    fun validate() {
//        components.values.forEach { it.validate(this) }
//    }

    /**
     * A list of modules that have an update available.
     */
    fun updatableModules(): List<Module> =
        modules.filter { it.isUpdateAvailable }


    val apiUrl: String = aplusUrl + "api/v2/"

    val courseApiUrl: String = apiUrl + "courses/" + id

    val requiredPlugins: List<PluginDependency> = emptyList()// TODO pluginDependencies

    val technicalDescription: String = "$name <$courseApiUrl>"

//    private val platformListener: PlatformListener = PlatformListener()

//    /**
//     * Constructor.
//     */
//    init {
//        this.platformListener = PlatformListener()
//        this.exerciseDataSource = APlusExerciseDataSource(
//            apiUrl, project.basePath
//                .resolve(Paths.get(Project.DIRECTORY_STORE_FOLDER, "a-plus-cache.json")), courseLastModified
//        )
//    }

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

//    fun getComponentIfExists(file: VirtualFile): OldComponent? {
//        val component = getComponentIfExists(file.name)
//        return if (component != null && component.fullPath == Paths.get(file.path)) component else null
//    }

//    fun register() {
//        ReadAction.run<RuntimeException> { platformListener.registerListeners() }
//    }
//
//    fun unregister() {
//        ReadAction.run<RuntimeException> { platformListener.unregisterListeners() }
//    }

//    private inner class PlatformListener {
//        private var messageBusConnection: MessageBusConnection? = null
//
//        private val libraryTableListener: LibraryTable.Listener = object : LibraryTable.Listener {
//            override fun afterLibraryRemoved(
//                library: com.intellij.openapi.roots.libraries.Library
//            ) {
//                library.name?.let { name ->
//                    getComponentIfExists(name).setUnresolved()
//                }
//            }
//        }
//
//        @RequiresReadLock
//        @Synchronized
//        fun registerListeners() {
//            if (messageBusConnection != null) {
//                return
//            }
//            messageBusConnection = project.messageBus.connect()
//
//            (messageBusConnection ?: return).subscribe(
//                VirtualFileManager.VFS_CHANGES,
//                object : BulkFileListener {
//                    override fun after(events: List<VFileEvent>) {
//                        for (event in events) {
//                            if (event is VFileDeleteEvent) {
//                                event.file.let { file ->
//                                    getComponentIfExists(file)?.setUnresolved()
//                                }
//                            }
//                        }
//                    }
//                }
//            )
//            (messageBusConnection ?: return).subscribe(ModuleListener.TOPIC, object : ModuleListener {
//                override fun moduleRemoved(
//                    project: Project,
//                    projectModule: com.intellij.openapi.module.Module
//                ) {
//                    projectModule.name.let { name ->
//                        getComponentIfExists(name).setUnresolved()
//                    }
//                }
//            })
//            project.libraryTable.addListener(libraryTableListener)
//        }
//
//        @RequiresReadLock
//        @Synchronized
//        fun unregisterListeners() {
//            if (messageBusConnection == null) {
//                return
//            }
//            (messageBusConnection ?: return).disconnect()
//            messageBusConnection = null
//            project.libraryTable.removeListener(libraryTableListener)
//        }
//    }
}
