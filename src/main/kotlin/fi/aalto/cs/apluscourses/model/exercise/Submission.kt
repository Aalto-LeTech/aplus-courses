package fi.aalto.cs.apluscourses.model.exercise

import java.nio.file.Path

/**
 * @param exercise Exercise.
 * @param files    Map from keys to file paths.
 * @param group    Group in which the submission is made.
 * @param language Language of the submission.
 */
data class Submission(
    val exercise: Exercise,
    val files: Map<String, Path>,
    val group: Group,
    val language: String
)
