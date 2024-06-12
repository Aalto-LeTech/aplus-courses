package fi.aalto.cs.apluscourses.model

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import fi.aalto.cs.apluscourses.intellij.model.APlusProject
import fi.aalto.cs.apluscourses.intellij.model.IntelliJComponent
import fi.aalto.cs.apluscourses.intellij.utils.ListDependenciesPolicy
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.PluginSettings.Companion.getInstance
import fi.aalto.cs.apluscourses.utils.Version
import fi.aalto.cs.apluscourses.utils.content.RemoteZippedDir
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.util.*

internal class IntelliJModule(
    name: String,
    url: URL,
    changelog: String,
    version: Version,
    localVersion: Version?,
    downloadedAt: ZonedDateTime?,
    val project: APlusProject,
    originalName: String = name
) : Module(name, url, changelog, version, localVersion, downloadedAt, originalName),
    IntelliJComponent<com.intellij.openapi.module.Module?> {
    override fun getPath(): Path {
        return Paths.get(name)
    }

    override fun getFullPath(): Path {
        return project.basePath.resolve(path)
    }

    @Throws(IOException::class)
    public override fun fetchInternal() {
        RemoteZippedDir(getUrl().toString(), getOriginalName())
            .copyTo(fullPath, project.project)
        if (!fullPath.resolve(getOriginalName() + ".iml").toFile().renameTo(imlFile)) {
            throw IOException("Could not rename iml file.")
        }

        val courseProject = getInstance().getCourseProject(project.project)
        courseProject?.course?.callbacks?.invokePostDownloadModuleCallbacks(project.project, this)
    }

    override fun resolveStateInternal(): Int {
        return project.resolveComponentState(this)
    }

    @Throws(ComponentLoadException::class)
    override fun load() {
        WriteAction.runAndWait<ComponentLoadException> { this.loadInternal() }
    }

    @RequiresWriteLock
    @Throws(ComponentLoadException::class)
    private fun loadInternal() {
        try {
            project.moduleManager.loadModule(imlFile.toPath())
            getInstance()
                .getCourseFileManager(project.project)
                .addModuleEntry(this)
        } catch (e: Exception) {
            throw ComponentLoadException(getName(), e)
        }
    }

    override fun unload() {
        super.unload()
        WriteAction.runAndWait<RuntimeException> { this.unloadInternal() }
    }

    @RequiresWriteLock
    private fun unloadInternal() {
        val module = platformObject
        if (module != null) {
            project.moduleManager.disposeModule(module)
        }
    }

    @Throws(IOException::class)
    override fun remove() {
        FileUtils.deleteDirectory(fullPath.toFile())
    }

    override fun getErrorCause(): Int {
        return if (!imlFile.exists()) ERR_FILES_MISSING else super.getErrorCause()
    }

    override fun computeDependencies(): List<String> {
        val moduleRootManager = project.getModuleRootManager(getName()) ?: throw IllegalStateException()
        return Objects.requireNonNull(
            moduleRootManager
                .orderEntries()
                .withoutSdk()
                .withoutModuleSourceEntries()
                .productionOnly()
                .process(ListDependenciesPolicy(), ArrayList())
        )
    }

    private val imlFile: File
        get() = fullPath.resolve(getName() + ".iml").toFile()

    @RequiresReadLock
    override fun getPlatformObject(): com.intellij.openapi.module.Module? {
        return project.moduleManager.findModuleByName(getName())
    }

    override fun hasLocalChanges(downloadedAt: ZonedDateTime): Boolean {
        val fullPath = fullPath
        val timeStamp = (downloadedAt.toInstant().toEpochMilli()
                + PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION)
        return ReadAction.compute<Boolean, RuntimeException> { VfsUtil.hasDirectoryChanges(fullPath, timeStamp) }
    }

    override fun copy(newName: String): Module {
        return IntelliJModule(newName, url, changelog, version, localVersion, downloadedAt, project, originalName)
    }
}
