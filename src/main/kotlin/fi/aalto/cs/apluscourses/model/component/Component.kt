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
        CoursesClient.getInstance(project).downloadAndUnzip(zipUrl, extractPath, onlyPath)
    }

    protected abstract fun findDependencies(): Set<String>

    abstract val platformObject: T?

    var status: Status = Status.NOT_LOADED
        protected set

    enum class Status {
        NOT_LOADED,
        LOADING,
        LOADED,
        ERROR
    }

    fun updateAndGetStatus(): Status {
        updateStatus()
        return status
    }

    abstract fun updateStatus()

    abstract suspend fun downloadAndInstall(updating: Boolean = false)
}