package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleOrderEntry
import com.intellij.openapi.roots.RootPolicy
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseFileManager.ModuleMetadata
import fi.aalto.cs.apluscourses.utils.Version
import java.nio.file.Path
import java.nio.file.Paths
import com.intellij.openapi.module.Module as IdeaModule

class Module(
    name: String,
    val zipUrl: String,
    val changelog: String?,
    val latestVersion: Version,
    project: Project
) : Component<IdeaModule>(name, project) {
    override val path: Path
        get() = Path.of(name)

    override val fullPath: Path
        get() = Path.of(project.basePath!!).resolve(path)

    override val platformObject: Module?
        get() = ModuleManager.getInstance(project).findModuleByName(name)

    val metadata: ModuleMetadata?
        get() = CourseFileManager.getInstance(project).getMetadata(name)

    override fun findDependencies(): Set<String> {
        if (status != Status.LOADED) return emptySet()
        val module = platformObject
        if (module == null) {
            status = Status.ERROR // TODO proper error
            return emptySet()
        }
        return module
            .rootManager
            .orderEntries()
            .withoutSdk()
            .withoutModuleSourceEntries()
            .productionOnly()
            .process(DependenciesPolicy(), emptySet())
    }

    fun setError() {
        status = Status.ERROR
    }

    val documentationIndexFullPath: Path
        get() = Paths.get(fullPath.toString(), "doc/index.html")

    val documentationExists: Boolean
        get() = status == Status.LOADED && documentationIndexFullPath.toFile().exists()

//    fun hasLocalChanges(downloadedAt: ZonedDateTime): Boolean {
//        val fullPath = fullPath
//        val timeStamp = (downloadedAt.toInstant().toEpochMilli()
//                + PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION)
//        return false
////        return ReadAction.compute<Boolean, RuntimeException> { VfsUtil.hasDirectoryChanges(fullPath, timeStamp) }
//    }

    override suspend fun downloadAndInstall() {
        if (platformObject != null) {
            return
        }
        status = Status.LOADING
        downloadAndUnzipZip(zipUrl, Path.of(project.basePath!!))
        CourseFileManager.getInstance(project).addModule(this)
        println("Loading module $name ${imlPath}")
        writeAction {
            ModuleManager.getInstance(project).loadModule(imlPath)
        }
        status = Status.LOADED
    }

    private val imlPath
        get() = fullPath.resolve("$name.iml")

    override suspend fun remove(deleteFiles: Boolean) {
        TODO("Not yet implemented")
    }

    override fun load() {
        /*
       * Four cases to check for here:
       *   0. The component is in the project, but the files are missing -> ERROR.
       *   1. The component is in the project, so its state should be INSTALLED.
       *   2. The component is not in the project, but the module files are present in the file
       *      system, so its state should be FETCHED.
       *   3. The component files aren't present in the file system (and by extension, the component
       *      isn't in the project), so its state should be NOT_INSTALLED.
       */
        val module = platformObject
        val exists = module != null
        val loaded = module != null && module.isLoaded
        if (loaded) {
            status = Status.LOADED
            return
        } else if (exists) {
            status = Status.ERROR
            return
        } else {
            status = Status.UNRESOLVED
            return
        }
//        val filesOk: Boolean = doesDirExist(component.path)
//        println("loaded: " + loaded + ", filesOk: " + filesOk + ", name: " + name)
//        if (loaded && !filesOk) {
//            return OldComponent.ERROR
//        }
//        if (loaded) {
//            return OldComponent.LOADED
//        }
//        if (filesOk) {
//            return OldComponent.FETCHED
//        }
//        return OldComponent.NOT_INSTALLED
    }

    val isUpdateAvailable: Boolean
        get() = metadata != null && latestVersion > metadata!!.version

    val isMinorUpdate: Boolean
        get() = isUpdateAvailable && latestVersion.major == metadata!!.version.major

    enum class Category {
        ACTION_REQUIRED, INSTALLED, AVAILABLE
    }

    val category: Category
        get() {
            return if (status == Status.LOADED) {
                Category.INSTALLED
//            } else if (status == Status.ERROR || dependencyState == OldComponent.DEP_ERROR || isUpdateAvailable) {
            } else if (status == Status.ERROR || isUpdateAvailable) {
                Category.ACTION_REQUIRED
            } else {
                Category.AVAILABLE
            }
        }
}

/**
 * This class is a [RootPolicy] that builds a set of the names of those
 * [com.intellij.openapi.roots.OrderEntry] objects that represents dependencies of an
 * [Component] object (that is, modules and non-module-level libraries).
 */
private class DependenciesPolicy : RootPolicy<Set<String>>() {
    override fun visitModuleOrderEntry(
        moduleOrderEntry: ModuleOrderEntry,
        entries: Set<String>
    ): Set<String> = entries + moduleOrderEntry.moduleName

    override fun visitLibraryOrderEntry(
        libraryOrderEntry: LibraryOrderEntry,
        entries: Set<String>
    ): Set<String> {
        val name = libraryOrderEntry.libraryName
        if (!libraryOrderEntry.isModuleLevel && name != null) return entries + name
        return entries
    }
}
