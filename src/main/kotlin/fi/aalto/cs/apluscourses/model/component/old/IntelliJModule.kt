package fi.aalto.cs.apluscourses.model.component.old

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.utils.Version
import io.ktor.http.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZonedDateTime

internal class IntelliJModule(
    name: String,
    url: Url,
    changelog: String,
    version: Version,
    localVersion: Version?,
    downloadedAt: ZonedDateTime,
    val project: Project,
    originalName: String = name
) : OldModule(name, url, changelog, version, localVersion, downloadedAt, originalName),
    IntelliJComponent<Module> {
    override val path: Path
        get() = Paths.get(originalName)

    override val fullPath: Path
        get() = path//project.basePath.resolve(path)

    @Throws(IOException::class)
    override fun fetchInternal() {
//        RemoteZippedDir(url.toString(), originalName)
//            .copyTo(fullPath, project.project)
//        if (!fullPath.resolve(originalName + ".iml").toFile().renameTo(imlFile)) {
//            throw IOException("Could not rename iml file.")
//        }

//        val courseProject = getInstance().getCourseProject(project.project)
//        courseProject?.course?.callbacks?.invokePostDownloadModuleCallbacks(project.project, this)
    }

    override fun resolveStateInternal(): Int {
        return 0
//        return project.resolveComponentState(this)
    }

    //    @Throws(ComponentLoadException::class)
    override fun load() {
//        WriteAction.runAndWait<ComponentLoadException> { this.loadInternal() }
    }

    //    @RequiresWriteLock
//    @Throws(ComponentLoadException::class)
    private fun loadInternal() {
        try {
//            project.moduleManager.loadModule(imlFile.toPath())
//            getInstance()
//                .getCourseFileManager(project.project)
//                .addModuleEntry(this)
        } catch (e: Exception) {
//            throw ComponentLoadException(originalName, e)
        }
    }

    override fun unload() {
//        super.unload()
//        WriteAction.runAndWait<RuntimeException> { this.unloadInternal() }
    }

    @RequiresWriteLock
    private fun unloadInternal() {
        val module = platformObject
        if (module != null) {
//            project.moduleManager.disposeModule(module)
        }
    }

    @Throws(IOException::class)
    override fun remove() {
        FileUtils.deleteDirectory(fullPath.toFile())
    }

    override val errorCause: Int
        get() = if (!imlFile.exists()) ERR_FILES_MISSING else super.errorCause


    override fun computeDependencies(): List<String> {
//        val moduleRootManager = project.getModuleRootManager(originalName) ?: throw IllegalStateException()
        return emptyList()
//        return Objects.requireNonNull(
//            moduleRootManager
//                .orderEntries()
//                .withoutSdk()
//                .withoutModuleSourceEntries()
//                .productionOnly()
//                .process(ListDependenciesPolicy(), ArrayList())
//        )
    }

    private val imlFile: File
        get() = fullPath.resolve("$originalName.iml").toFile()

    override val platformObject
        get() = null//project.moduleManager.findModuleByName(originalName)

    override fun hasLocalChanges(downloadedAt: ZonedDateTime): Boolean {
        val fullPath = fullPath
        val timeStamp = (downloadedAt.toInstant().toEpochMilli()
                + PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION)
        return false
//        return ReadAction.compute<Boolean, RuntimeException> { VfsUtil.hasDirectoryChanges(fullPath, timeStamp) }
    }

    override fun copy(newName: String): OldModule {
        return IntelliJModule(
            newName, url, changelog, version, localVersion, downloadedAt, project,
            originalName
        )
    }
}
