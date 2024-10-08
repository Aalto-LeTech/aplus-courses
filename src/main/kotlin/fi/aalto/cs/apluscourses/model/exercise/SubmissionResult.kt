package fi.aalto.cs.apluscourses.model.exercise

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.api.APlusApi
import org.jetbrains.annotations.NonNls

data class SubmissionResult(
    val id: Long,
    val url: String,
    val maxPoints: Int,
    var userPoints: Int,
    var latePenalty: Double?,
    var status: Status,
    val filesInfo: List<SubmissionFileInfo>,
    val submitters: List<Long>?,
    val testsSucceeded: Int = -1,
    val testsFailed: Int = -1, // TODO
) {
    suspend fun getHtmlUrl(project: Project): String {
        return APlusApi.submission(this).get(project).htmlUrl
    }

    fun updateStatus(statusString: String) {
        val status = statusFromString(statusString)
        this.status = status
    }

    enum class Status {
        GRADED,
        UNOFFICIAL,
        REJECTED,
        WAITING,
        UNKNOWN,
    }

    companion object {
        fun statusFromString(@NonNls statusString: String?): Status {
            return when (statusString) {
                "ready" -> Status.GRADED
                "unofficial" -> Status.UNOFFICIAL
                "rejected" -> Status.REJECTED
                "waiting" -> Status.WAITING
                else -> Status.UNKNOWN
            }
        }
    }
}
