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
    var tags: List<String>,
    var status: Status,
    val filesInfo: List<SubmissionFileInfo>,
    val isSubmittable: Boolean,
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

    fun hasTag(tagSlug: String): Boolean {
        return tags.contains(tagSlug)
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
