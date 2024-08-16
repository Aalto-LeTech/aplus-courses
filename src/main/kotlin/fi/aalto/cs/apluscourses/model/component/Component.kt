package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.services.CoursesClient
import java.nio.file.Path

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
        CoursesClient.getInstance(project).getAndUnzip(zipUrl, extractPath, onlyPath)
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

    abstract suspend fun downloadAndInstall(updating: Boolean = false)

    abstract suspend fun remove(deleteFiles: Boolean)
}