package fi.aalto.cs.apluscourses.model.exercise

import kotlinx.serialization.Serializable
import java.util.*
import java.util.regex.Pattern

@Serializable
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
class Points
/**
 * Construct an instance with the given maps.
 *
 * @param exercises       A map of exercise group IDs to a list of exercises for that exercise
 * group.
 * @param submissions     A map of exercise IDs to a list of submission IDs for that exercise.
 * The first element of the list should be the ID of the oldest submission
 * and the last element should be the ID of the latest submission.
 * @param bestSubmissions A map of exercise IDs to the IDs of the best submission for each
 * exercise.
 */(
    private val exercises: Map<Long, List<Long>>,
    private val submissions: Map<Long, List<Long>>,
    private val bestSubmissions: Map<Long, Long>
) {
    fun exercisesCount(): Int = exercises.size

    /**
     * Returns the amount of exercises.
     */
//        get() {
//            if (field == null) {
//                field =
//                    exercises.values.stream().mapToInt(ToIntFunction<List<Long>> { obj: List<Long?> -> obj.size }).sum()
//            }
//            return field
//        }
//        private set
    fun submissionsCount(): Int = submissions.size

    /**
     * Returns the amount of exercises.
     */
//        get() {
//            if (field == null) {
//                field = submissions.values.stream().mapToInt(ToIntFunction<List<Long>> { obj: List<Long?> -> obj.size })
//                    .sum()
//            }
//            return field
//        }
//        private set

    fun getSubmissions(exerciseId: Long): List<Long> {
        return submissions.getOrDefault(exerciseId, emptyList())
    }

    fun getSubmissionsAmount(id: Long): Int {
        return submissions[id]!!.size
    }

    fun getExercises(exerciseGroupId: Long): List<Long> {
        return exercises.getOrDefault(exerciseGroupId, emptyList())
    }

    val bestSubmissionIds: Map<Long, Long>
        get() = Collections.unmodifiableMap(bestSubmissions)

    companion object {
        private val submissionIdPattern: Pattern = Pattern.compile("/submissions/(\\d+)/?$")

        /**
         * Constructs a [Points] instance from the given JSON object.
         *
         * @param jsonObject The JSON object from which the [Points] instance is constructed.
         */
//        fun fromJsonObject(jsonObject: JSONObject): Points {
//            val modulesArray: JSONArray = jsonObject.getJSONArray("modules")
//            val exercises: MutableMap<Long, List<Long>> = HashMap()
//            val submissions: MutableMap<Long, List<Long>> = HashMap()
//            val bestSubmissions: MutableMap<Long, Long> = HashMap()
//            for (i in 0 until modulesArray.length()) {
//                val module: JSONObject = modulesArray.getJSONObject(i)
//                val exerciseGroupId: Unit = module.getLong("id")
//                val exerciseIds: MutableList<Long> = ArrayList()
//                val exercisesArray: JSONArray = module.getJSONArray("exercises")
//                for (j in 0 until exercisesArray.length()) {
//                    val exercise: JSONObject = exercisesArray.getJSONObject(j)
//                    val exerciseId: Long = exercise.getLong("id")
//                    exerciseIds.add(exerciseId)
//                    parseSubmissions(exercise, exerciseId, submissions)
//
//                    val bestSubmissionId = parseSubmissionId(exercise.optString("best_submission"))
//                    if (bestSubmissionId != null) {
//                        bestSubmissions[exerciseId] = bestSubmissionId
//                    }
//                }
//                exercises[exerciseGroupId] = exerciseIds
//            }
//            return Points(exercises, submissions, bestSubmissions)
//        }

        /*
   * Parses the submissions (IDs and points) from the given JSON and adds them to the given maps.
   */
//        private fun parseSubmissions(
//            exerciseJson: JSONObject,
//            exerciseId: Long,
//            submissions: MutableMap<Long, List<Long>>
//        ) {
//            val submissionsArray: JSONArray = exerciseJson.getJSONArray("submissions_with_points")
//            val submissionIds: MutableList<Long> = ArrayList<Any?>(submissionsArray.length())
//            for (i in submissionsArray.length() - 1 downTo 0) {
//                val submission: JSONObject = submissionsArray.getJSONObject(i)
//                val submissionId: Long = submission.getLong("id")
//                submissionIds.add(submissionId)
//            }
//            submissions[exerciseId] = submissionIds
//        }

        private fun parseSubmissionId(submissionUrl: String): Long? {
            val matcher = submissionIdPattern.matcher(submissionUrl)
            if (!matcher.find()) {
                return null
            }
            return matcher.group(1).toLong()
        }
    }
}
