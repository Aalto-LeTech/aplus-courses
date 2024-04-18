package fi.aalto.cs.apluscourses.model.exercise

import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: Long,
    val name: String,
    val htmlUrl: String,
    val url: String,
    var submissionInfo: SubmissionInfo?,
    val submissionResults: MutableList<SubmissionResult>,
    val maxPoints: Int,
    val userPoints: Int,
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
    fun isInGrading(): Boolean = submissionResults.any { it.status == SubmissionResult.Companion.Status.WAITING }

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


//    companion object {
//        /**
//         * Construct an exercise from the given JSON object. The object must contain an integer value for
//         * the key "id", a string value for the key "display_name", a string value for the key "html_url",
//         * and integer values for the keys "max_points" and "max_submissions".
//         *
//         * @param jsonObject The JSON object from which the exercise is constructed.
//         * @return An exercise instance.
//         */
//        fun fromJsonObject(
//            jsonObject: JSONObject,
//            points: Points,
//            optionalCategories: Set<String?>,
//            languageCode: String
//        ): Exercise {
//            val id = jsonObject.getLong("id")
//
//            val name = APlusLocalizationUtil.getLocalizedName(jsonObject.getString("display_name"), languageCode)
//            val htmlUrl = jsonObject.getString("html_url")
//
//            val bestSubmissionId = points.bestSubmissionIds[id]
//            val maxPoints = jsonObject.getInt("max_points")
//            val maxSubmissions = jsonObject.getInt("max_submissions")
//            val difficulty = jsonObject.optString("difficulty")
//            val isOptional = optionalCategories.contains(difficulty)
//
//            val submissionInfo = SubmissionInfo.fromJsonObject(jsonObject)
//
//            val optionalBestSubmission = if (bestSubmissionId == null) OptionalLong.empty()
//            else OptionalLong.of(bestSubmissionId)
//
//            return Exercise(
//                id, name, htmlUrl, submissionInfo, maxPoints, maxSubmissions, optionalBestSubmission,
//                difficulty, isOptional
//            )
//        }
//    }
}
