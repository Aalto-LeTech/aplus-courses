package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.DownloadProgress
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

    protected suspend fun downloadAndUnzipZip(
        zipUrl: String,
        extractPath: Path,
        onlyPath: String? = null,
        onProgress: DownloadProgress? = null,
    ) {
        CoursesClient.getInstance(project).downloadAndUnzip(zipUrl, extractPath, onlyPath, onProgress)
    }

    protected suspend fun downloadFile(url: String, target: Path) {
        CoursesClient.getInstance(project).downloadFile(url, target)
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

    /**
     * @param trackSetup whether to list this install on the course setup checklist,
     *   true for components required by courses.
     */
    abstract suspend fun downloadAndInstall(updating: Boolean = false, trackSetup: Boolean = false)
}