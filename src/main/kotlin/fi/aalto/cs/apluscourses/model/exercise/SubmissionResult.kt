package fi.aalto.cs.apluscourses.model.exercise

import org.jetbrains.annotations.NonNls
import fi.aalto.cs.aplus.client.dto.SubmissionStatusDto

data class SubmissionResult(
    val id: Long,
    val maxPoints: Int,
    var userPoints: Int,
    var latePenalty: Double?,
    var status: Status,
    val isSubmittable: Boolean,
    val submitters: List<Long>?,
) {
    enum class Status {
        GRADED,
        UNOFFICIAL,
        REJECTED,
        ERROR,
        INVALIDATED,
        WAITING,
        UNKNOWN,
    }

    companion object {
        /**
         * Maps A+ submission statuses to UI statuses.
         *
         * `waiting` and `initialized` both keep grading polling active.
         * `unofficial` is terminal and graded, but its points do not count.
         */
        fun statusFromString(@NonNls statusString: String?): Status {
            return when (SubmissionStatusDto.parse(statusString)) {
                SubmissionStatusDto.READY -> Status.GRADED
                SubmissionStatusDto.UNOFFICIAL -> Status.UNOFFICIAL
                SubmissionStatusDto.REJECTED -> Status.REJECTED
                SubmissionStatusDto.ERROR -> Status.ERROR
                SubmissionStatusDto.INVALIDATED -> Status.INVALIDATED
                SubmissionStatusDto.WAITING, SubmissionStatusDto.INITIALIZED -> Status.WAITING
                null -> Status.UNKNOWN
            }
        }
    }
}
