package fi.aalto.cs.apluscourses.model.exercise

//import fi.aalto.cs.apluscourses.utils.JsonUtil
import kotlinx.serialization.Serializable

@Serializable
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
data class SubmissionResult(
    val id: Long,
    val url: String,
    val maxPoints: Int,
    val userPoints: Int,
    var latePenalty: Double?,
    var status: Status,
    val filesInfo: List<SubmissionFileInfo>,
    val testsSucceeded: Int = -1,
    val testsFailed: Int = -1,
    var isDetailsLoaded: Boolean = false
//) : Browsable {
) {
//    fun getFilesInfo(): Array<SubmissionFileInfo> {
//        return filesInfo
//    }

    fun updateStatus(statusString: String) {
        val status = when (statusString) {
            "ready" -> Status.GRADED
            "unofficial" -> Status.UNOFFICIAL
            "waiting" -> Status.WAITING
            else -> Status.UNKNOWN
        }
        this.status = status
    }


    companion object {
        enum class Status {
            UNKNOWN,
            GRADED,
            UNOFFICIAL,
            WAITING
        }
    }

    /**
     * Construct an instance with the given ID and exercise URL.
     */

//    val htmlUrl: String
//        get() = exercise.htmlUrl + "submissions/" + id + "/"

//    companion object {
//        /**
//         * Construct a [SubmissionResult] instance from the given JSON object. The JSON object must
//         * contain an integer for the "id" key, and optionally a string value for the "status" key.
//         */
//        fun fromJsonObject(
//            jsonObject: JSONObject,
//            exercise: Exercise,
//            course: Course
//        ): SubmissionResult {
//            val id: Long = jsonObject.getLong("id")
//            val points: Int = jsonObject.getInt("grade")
//            val latePenalty: Double = jsonObject.optDouble("late_penalty_applied", 0.0)
//
//            var status = Status.UNKNOWN
//            val statusString: String = jsonObject.optString("status")
//            if ("ready" == statusString) {
//                status = Status.GRADED
//            } else if ("unofficial" == statusString) {
//                status = Status.UNOFFICIAL
//            } else if ("waiting" == statusString) {
//                status = Status.WAITING
//            }
//
//            val filesInfo: Unit = JsonUtil.parseArray(
//                jsonObject.getJSONArray("files"),
//                JSONArray::getJSONObject,
//                SubmissionFileInfo::fromJsonObject
//            ) { _Dummy_.__Array__() }
//
//            var feedbackParser: FeedbackParser = FeedbackParser()
//
//            if (course.getFeedbackParser() != null && exercise.isSubmittable && jsonObject.has("feedback")) {
//                when (course.getFeedbackParser()) {
//                    O1FeedbackParser.NAME -> feedbackParser = O1FeedbackParser()
//                    S2FeedbackParser.NAME -> feedbackParser = S2FeedbackParser()
//                    else -> {}
//                }
//            }
//
//            val testResults: Unit = feedbackParser.parseTestResults(jsonObject.optString("feedback", ""))
//
//            return SubmissionResult(
//                id, points, latePenalty, status, exercise, filesInfo, testResults.succeeded,
//                testResults.failed
//            )
//        }
//    }
}
