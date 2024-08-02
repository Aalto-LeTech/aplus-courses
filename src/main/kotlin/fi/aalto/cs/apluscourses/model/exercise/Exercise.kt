package fi.aalto.cs.apluscourses.model.exercise

import fi.aalto.cs.apluscourses.model.component.Module

class Exercise(
    val id: Long,
    val name: String,
    val module: Module?,
    val htmlUrl: String,
    val url: String,
    var submissionInfo: SubmissionInfo?,
    var submissionResults: MutableList<SubmissionResult>,
    val maxPoints: Int,
    var userPoints: Int,
    val maxSubmissions: Int,
    val bestSubmissionId: Long?,
    val difficulty: String = "",
    val isOptional: Boolean,
    val isSubmittable: Boolean,
    var isDetailsLoaded: Boolean = false
//) : Browsable {
) {
//    val submissionResults: MutableList<SubmissionResult> = Collections.synchronizedList(ArrayList())


//    override fun getHtmlUrl(): String {
//        return htmlUrl
//    }

    fun addSubmissionResult(submissionResult: SubmissionResult) {
        submissionResults.add(submissionResult)
    }


//    fun isSubmittable(): Boolean = submissionInfo?.isSubmittable() ?: false

    /**
     * Returns true if assignment is completed.
     *
     * @return True if userPoints are the same as maxPoints, otherwise False
     */
    fun isCompleted(): Boolean = userPoints == maxPoints && !isOptional
    // Optional assignments are never completed, since they can be filtered separately
    // and we can't tell from the points whether the submission was correct or not

    /**
     * Returns true if any of the submissions of this exercise has status WAITING.
     */
    fun isInGrading(): Boolean = submissionResults.any { it.status == SubmissionResult.Status.WAITING }

    /**
     * Returns the best submission of this exercise (if one exists).
     */
    fun bestSubmission(): SubmissionResult? {
        return submissionResults.find { it.id == bestSubmissionId }
    }

    fun isLate(): Boolean {
        val bestSubmission = bestSubmission()
        return bestSubmission?.latePenalty != null && bestSubmission.latePenalty != 0.0
    }

    override fun toString(): String = name
    override fun equals(other: Any?): Boolean = other is Exercise && other.id == id
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (module?.hashCode() ?: 0)
        result = 31 * result + htmlUrl.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (submissionInfo?.hashCode() ?: 0)
        result = 31 * result + submissionResults.hashCode()
        result = 31 * result + maxPoints
        result = 31 * result + userPoints
        result = 31 * result + maxSubmissions
        result = 31 * result + (bestSubmissionId?.hashCode() ?: 0)
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + isOptional.hashCode()
        result = 31 * result + isSubmittable.hashCode()
        result = 31 * result + isDetailsLoaded.hashCode()
        return result
    }
}
