package fi.aalto.cs.apluscourses.model.exercise

import fi.aalto.cs.apluscourses.model.component.Module

data class Exercise(
    val id: Long,
    val name: String,
    val module: Module?,
    val htmlUrl: String,
    val url: String,
    val submissionResults: MutableList<SubmissionResult>,
    val maxPoints: Int,
    val userPoints: Int,
    val maxSubmissions: Int,
    val bestSubmissionId: Long?,
    val difficulty: String = "",
    val isOptional: Boolean,
    val isSubmittable: Boolean,
    val isFeedback: Boolean
) {

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
    private fun bestSubmission(): SubmissionResult? { //this may need some adjustments
        //aina oletuksena se, jonka api saa palvelimelta, mutta jos löytyy ilman varoitustunnistetta, välitä se eteenpäin
        val best = submissionResults.find { it.id == bestSubmissionId }
        return submissionResults.find { !it.hasTag("warn") && it.userPoints >= (best?.userPoints ?: 0) } ?: best
    }

    fun bestHasWarn(): Boolean {
        val bestSubmission = bestSubmission()
        return bestSubmission != null && bestSubmission.hasTag("warn")
    }

    fun isLate(): Boolean {
        val bestSubmission = bestSubmission()
        return bestSubmission?.latePenalty != null && bestSubmission.latePenalty != 0.0
    }

    override fun toString(): String = name

}
