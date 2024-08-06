package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.services.CoursesClient
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.sequences.forEach

abstract class Component<T>(val name: String, protected val project: Project) {
    var dependencyNames: Set<String>? = null
        private set
        get() {
            if (platformObject == null) {
                return null
            }
            if (field == null) {
                field = findDependencies()
            }
            return field
        }

    abstract val path: Path
    abstract val fullPath: Path

    protected suspend fun downloadAndUnzipZip(zipUrl: String, extractPath: Path, onlyPath: String? = null) {
        val res = CoursesClient.getInstance(project).get(zipUrl)
        println("Downloading $zipUrl to $path")
        val zipBytes = res.readBytes()
        val tempZipFile = kotlin.io.path.createTempFile("aplus", ".zip").toFile()
        tempZipFile.writeBytes(zipBytes)
        println("Downloaded $zipUrl to $tempZipFile")
        val destination = extractPath
        val destinationFile = destination.toFile()
        withContext(Dispatchers.IO) {
            if (!destinationFile.exists()) {
                destinationFile.mkdirs()
            }
            ZipFile(tempZipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { inputStream ->
                        val file = destination.resolve(entry.name).toFile()
                        if (onlyPath != null && !file.path.contains(onlyPath)) {
                            return@use
                        }
                        println("Extracting ${entry.name} to $file")
                        if (entry.isDirectory) {
                            file.mkdir()
                        } else {
                            if (!file.parentFile.exists()) {
                                file.parentFile.mkdirs()
                            }
                            file.writeBytes(inputStream.readBytes())
                        }

                    }

                }
            }
        }
        println("Extracted $zipUrl to $destination")
    }

    protected abstract fun findDependencies(): Set<String>

    abstract val platformObject: T?

    var status: Status = Status.UNRESOLVED
        protected set

    enum class Status {
        UNRESOLVED,
        LOADING,
        LOADED,
        ERROR
    }

    fun loadAndGetStatus(): Status {
        load()
        return status
    }

    abstract fun load()

    abstract suspend fun downloadAndInstall()

    abstract suspend fun remove(deleteFiles: Boolean)
}