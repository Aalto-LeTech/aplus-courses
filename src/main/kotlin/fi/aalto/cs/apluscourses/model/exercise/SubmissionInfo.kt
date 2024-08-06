package fi.aalto.cs.apluscourses.model.exercise

import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import kotlinx.serialization.Serializable

@Serializable
data class SubmissionInfo(val files: Map<String, List<SubmittableFile>>) {
//    private val files: Map<String, List<SubmittableFile>> = files

    /**
     * Return the submittable files corresponding to the given language (or an empty collection if
     * the language isn't found).
     */
    fun getFiles(language: String): List<SubmittableFile> {
        return files.getOrDefault(language, emptyList<SubmittableFile>())
    }

    /**
     * Returns true if there is some language in which the exercise can be submitted from the IDE.
     */
    fun isSubmittable(): Boolean = files.isNotEmpty()


    /**
     * Returns true if the exercise can be submitted in the given language from the IDE.
     */
    fun isSubmittable(language: String): Boolean {
        return getFiles(language).isNotEmpty()
    }

    companion object {
        /**
         * Construct a submission info instance from the given JSON object.
         */
        fun fromJsonObject(jsonObject: APlusApi.Exercise.Body): SubmissionInfo {

            // Some assignments, such as https://plus.cs.aalto.fi/api/v2/exercises/24882/ don't have the
            // exercise info at all.
            val exerciseInfo = jsonObject.exerciseInfo ?: return SubmissionInfo(emptyMap())

            // Some assignments, such as https://plus.cs.aalto.fi/api/v2/exercises/50181/ don't have the
            // form_spec field despite having exercise_info.
            val formSpec = exerciseInfo.formSpec ?: return SubmissionInfo(emptyMap())


            val localizationInfo = exerciseInfo.formI18n
            val files = mutableMapOf<String, MutableList<SubmittableFile>>()

            formSpec.filter { it.type == "file" }.forEach { spec ->
                val key = spec.key
                val title = spec.title
                val localizedFilenames = localizationInfo[title] ?: emptyMap()

                localizedFilenames.forEach { (language, filename) ->
                    val filesForLanguage = files.getOrPut(language) { mutableListOf() }
                    filesForLanguage.add(SubmittableFile(key, filename))
                }
            }

            return SubmissionInfo(files)
        }
    }
}
