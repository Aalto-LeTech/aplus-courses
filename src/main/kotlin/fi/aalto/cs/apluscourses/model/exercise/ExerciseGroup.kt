package fi.aalto.cs.apluscourses.model.exercise

import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.Converter
//import fi.aalto.cs.apluscourses.utils.JsonUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
//import org.json.JSONArray
//import org.json.JSONObject
import java.util.*


@Serializable
data class ExerciseGroup(
    val id: Long,
    val name: String,
    val maxPoints: Int,
    val userPoints: Int,
    val htmlUrl: String,

    val isOpen: Boolean,
//    val dummyExercises: List<DummyExercise>?,
    private val exerciseOrder: List<Long>,
    val exercises: MutableList<Exercise>
//) : Browsable {
) {
//
//    override fun isEqualToDefault(): Boolean {
//        return false
//    }

    companion object {
        private val json = Json


        class EGLConverter : com.intellij.util.xmlb.Converter<MutableList<ExerciseGroup>>() {
            override fun fromString(value: String): MutableList<ExerciseGroup>? {
                return json.decodeFromString(value)
            }

            override fun toString(value: MutableList<ExerciseGroup>): String? {
                return json.encodeToString(value)
            }
        }

        class EGConverter : Converter<ExerciseGroup>() {
            override fun toString(t: ExerciseGroup?, context: ConvertContext?): String =
                if (t == null) "" else json.encodeToString(t)

            override fun fromString(s: String?, context: ConvertContext?): ExerciseGroup? {
                return if (s == null) null else json.decodeFromString(s)
            }
        }
    }
//    class ExerciseGroup : BaseState() {
//        var id by property(0L)
//
//        var name by string()
//
//        var maxPoints by property(0)
//
//        var userPoints by property(0)
//
//        var htmlUrl by string()
//
//        var isOpen by property(false)
//
//        var exerciseOrder: MutableList<Long> by list()
//
//        @get:XCollection(style = XCollection.Style.v2)
//        var exercises: MutableList<Exercise> by list()
//    val exercises: MutableList<Exercise> = Collections.synchronizedList(ArrayList())

    /**
     * Construct an exercise group with the given name and exercises.
     */
    init {
//        exercises.addAll(dummyExercises!!)
//        sort()
    }

    private fun sort() {
        exercises.sortWith(Comparator.comparing { exercise: Exercise -> exerciseOrder.indexOf(exercise.id) })
    }

//    override fun getHtmlUrl(): String {
//        return htmlUrl
//    }

//    fun getExercises(): List<Exercise> {
//        return Collections.unmodifiableList(exercises)
//    }

//    data class Points(val userPoints: Int, val maxPoints: Int)
//
//    fun getPoints(): Points {
//        val maxPoints = exercises.sumOf { exercise -> exercise.maxPoints }
//        val userPoints = exercises.sumOf { exercise -> exercise.userPoints }
//        return Points(userPoints, maxPoints)
//    }

    /**
     * Adds an exercise or replaces an existing one.
     */
    fun addExercise(exercise: Exercise) {
        val oldExercise = exercises.stream().filter { oldEx: Exercise -> oldEx == exercise }.findFirst()
        oldExercise.ifPresent { o: Exercise -> exercises.remove(o) }
        val index = exerciseOrder.indexOf(exercise.id)
        if (index == -1) {
            exercises.add(exercise)
        } else if (index > exercises.size - 1) {
            exercises.add(exercise)
            sort()
        } else {
            exercises.add(index, exercise)
        }
    }

//    companion object {
//        /**
//         * Construct an exercise group from the given JSON object. The JSON object must contain a long
//         * with the key "id", a string with the key "display_name", and an array with the key "exercises".
//         * Each of the JSON objects in the exercise array is given to [Exercise.fromJsonObject].
//         *
//         * @param jsonObject The JSON object from which the exercise group is constructed.
//         */
//        fun fromJsonObject(
//            jsonObject: JSONObject,
//            exerciseOrder: Map<Long?, List<Long>>,
//            languageCode: String,
//            hiddenElements: CourseHiddenElements
//        ): ExerciseGroup {
//            val id = jsonObject.getLong("id")
//            val name = APlusLocalizationUtil.getLocalizedName(jsonObject.getString("display_name"), languageCode)
//            val htmlUrl = jsonObject.getString("html_url")
//            val isOpen = jsonObject.getBoolean("is_open")
//            val exercisesArray = jsonObject.getJSONArray("exercises")
//            val dummyExercises = JsonUtil
//                .parseArray(
//                    exercisesArray,
//                    { obj: JSONArray, index: Int ->
//                        obj.getJSONObject(index)
//                    },
//                    { obj: JSONObject -> DummyExercise.fromJsonObject(obj, languageCode) },
//                    { len: Int -> arrayOfNulls<DummyExercise>(len) }
//                )
//                .map { e: DummyExercise? -> e!! }
//                .filter { e: DummyExercise -> !hiddenElements.shouldHideObject(e.id, e.name, languageCode) }
//                .toList()
//            return ExerciseGroup(id, name, htmlUrl, isOpen, dummyExercises, exerciseOrder[id]!!)
//        }
//    }
}
