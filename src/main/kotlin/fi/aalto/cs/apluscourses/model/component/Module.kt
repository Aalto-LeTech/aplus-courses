package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleOrderEntry
import com.intellij.openapi.roots.RootPolicy
import com.intellij.util.io.createParentDirectories
import fi.aalto.cs.apluscourses.notifications.ModuleUpdatedNotification
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseFileManager.ModuleMetadata
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.ui.module.UpdateModuleDialog
import fi.aalto.cs.apluscourses.utils.FileUtil
import fi.aalto.cs.apluscourses.utils.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import com.intellij.openapi.module.Module as IdeaModule

class Module(
    name: String,
    val zipUrl: String,
    val changelog: String?,
    val latestVersion: Version,
    val language: String?,
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
        val module = platformObject
        if (module == null) return emptySet()
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

    fun changedFiles(): List<Path> {
        val fullPath = fullPath
        val timestamp = metadata?.downloadedAt ?: return emptyList()
        val timeStamp = timestamp.toEpochMilliseconds() + PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION
        return ReadAction.compute<List<Path>, RuntimeException> {
            FileUtil.getChangedFilesInDirectory(
                fullPath.toFile(),
                timeStamp
            )
        }
    }

    override suspend fun downloadAndInstall(updating: Boolean) {
        val oldPlatformObject = platformObject
        if (oldPlatformObject != null) {
            if (!updating) {
                return
            }
            writeAction {
                ModuleManager.getInstance(project).disposeModule(oldPlatformObject)
            }
        }
        status = Status.LOADING
        downloadAndUnzipZip(zipUrl, Path.of(project.basePath!!))
        CourseFileManager.getInstance(project).addModule(this)
        println("Loading module $name $imlPath")
        writeAction {
            ModuleManager.getInstance(project).loadModule(imlPath)
        }
        status = Status.LOADED
        val initialReplCommands = CourseManager.course(project)?.replInitialCommands?.get(name)
        val platformModule = platformObject
        if (initialReplCommands != null && platformModule != null) {
            platformModule.guessModuleDir()?.toNioPath()?.resolve(".repl-commands")?.toFile()
                ?.writeText(initialReplCommands.joinToString("\n"))
        }
    }

    suspend fun update() {
        val filesWithChanges = changedFiles()
        val allFiles = FileUtil.getAllFilesInDirectory(fullPath.toFile())
        if (filesWithChanges.isNotEmpty()) {
            val canceled = withContext(Dispatchers.EDT) {
                !UpdateModuleDialog(project, this@Module, filesWithChanges).showAndGet()
            }
            if (canceled) return
            val backupDir = fullPath.resolve("backup")

            for (file in filesWithChanges) {
                val relativePath = fullPath.relativize(file)
                val targetPath = backupDir.resolve(relativePath)

                if (!targetPath.parent.exists()) {
                    targetPath.createParentDirectories()
                }
                file.moveTo(targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        FileUtil.deleteFilesInDirectory(fullPath.toFile(), fullPath.resolve("backup"))
        downloadAndInstall(updating = true)

        val newFiles = FileUtil.getAllFilesInDirectory(fullPath.toFile())
        val deletedFiles = allFiles - newFiles
        val addedFiles = newFiles - allFiles
        Notifier.notifyAndHide(
            ModuleUpdatedNotification(this, addedFiles, deletedFiles),
            project
        )
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
        get() = status == Status.LOADED && metadata != null && latestVersion > metadata!!.version

    val isMinorUpdate: Boolean
        get() = isUpdateAvailable && latestVersion.major == metadata!!.version.major

    enum class Category {
        ACTION_REQUIRED, INSTALLED, AVAILABLE
    }

    val category: Category
        get() {
            return if (status == Status.ERROR || isUpdateAvailable) {
                Category.ACTION_REQUIRED
            } else if (status == Status.LOADED) {
                Category.INSTALLED
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
private class DependenciesPolicy : RootPolicy<Set<String>> {
    constructor()

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
