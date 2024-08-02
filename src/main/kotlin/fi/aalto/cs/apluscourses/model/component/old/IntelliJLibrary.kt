package fi.aalto.cs.apluscourses.model.component.old

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.libraries.LibraryProperties
import com.intellij.openapi.roots.libraries.PersistentLibraryKind
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

abstract class IntelliJLibrary
<K : PersistentLibraryKind<out LibraryProperties<S>?>?, S>
protected constructor(name: String, protected val project: Project) : OldLibrary(name),
    IntelliJComponent<Library?> {
    @Throws(IOException::class)
    override fun fetch() {
//        for (content in contents) {
//            content.copyTo(fullPath, project)
//        }
    }

//    protected abstract val contents: Array<Content>

    @RequiresWriteLock
    protected fun loadInternal() {
//        val libraryTable = project.libraryTable.modifiableModel
//        val library = libraryTable
//            .createLibrary(originalName, libraryKind)
//            .modifiableModel
//        for (uri in classUris) {
//            library.addRoot(uri, OrderRootType.CLASSES)
//        }

        //HACK: this is the only way to access properties that I am aware of
//        val libraryEx = library as ModifiableModelEx
//        val properties: LibraryProperties<S>? = libraryEx.properties as LibraryProperties<S>?
//        if (properties != null) {
//            val newState = getPropertiesState(properties.state)
//            if (newState != null) {
//                properties.loadState(newState)
//                libraryEx.properties = properties
//            }
//        }

//        library.commit()
//        libraryTable.commit()
//        VirtualFileManager.getInstance().syncRefresh()
    }

    override fun load() {
        WriteAction.runAndWait<RuntimeException> { this.loadInternal() }
    }

    override fun unload() {
        super.unload()
        WriteAction.runAndWait<RuntimeException> { this.unloadInternal() }
    }

    @RequiresWriteLock
    private fun unloadInternal() {
//        val libraryTable = project.libraryTable
//        val library: com.intellij.openapi.roots.libraries.Library = platformObject
//        if (library != null) {
//            libraryTable.removeLibrary(library)
//        }
    }

    @Throws(IOException::class)
    override fun remove() {
        FileUtils.deleteDirectory(fullPath.toFile())
    }

    override val path: Path
        get() = Paths.get("lib", originalName)

    override val fullPath: Path
        get() = Paths.get(project.basePath!!).resolve(path)

    override fun resolveStateInternal(): Int {
        return 0
//        return project.resolveComponentState(this)
    }

    @get:RequiresReadLock
    protected val classUris: List<String>
        /**
         * URIs MUST NOT be escaped!  Note that java.net.URI does escaping so avoid using that.
         *
         * @return URIs to be included in classes of the library.
         */
        get() = getUris(classRoots) { path: Path -> VfsUtil.getUrlForLibraryRoot(path.toFile()) }

    protected fun getUris(roots: List<String>, pathToUri: (Path) -> String): List<String> {
        return roots
            .filter { string: String -> string.isNotEmpty() }
            .map { other: String -> fullPath.resolve(other) }
            .map { pathToUri(it) }
    }

    /**
     * Gets local file system URIs of the given roots.
     *
     * @param roots File names for library class roots.
     * @return An array of URI strings (unescaped).
     */
    @RequiresReadLock
    protected fun getUris(roots: List<String>): List<String> {
        val protocol = LocalFileSystem.getInstance().protocol
        return getUris(roots) { path: Path ->
            VirtualFileManager.constructUrl(
                protocol,
                FileUtil.toSystemIndependentName(path.toString())
            )
        }
    }

    protected open val classRoots: List<String>
        get() = jarFiles // use all JAR files by default

    protected open val libraryKind: K?
        get() = null // default to null ("normal")

    protected val jarFiles: List<String>
        /**
         * Helper method that returns all the JAR files in the library path.
         *
         * @return An array of filenames.
         */
        get() {
            val files = fullPath.toFile().listFiles()!!
            return files
                .map { obj: File -> obj.name }
                .filter { fileName: String -> fileName.endsWith(".jar") }
        }

    protected open fun getPropertiesState(currentState: S?): S? {
        return currentState
    }


    @get:RequiresReadLock
    override val platformObject
        get() = null//project.libraryTable.getLibraryByName(originalName)
}
