package fi.aalto.cs.apluscourses.model.exercise

import kotlinx.serialization.Serializable

@Serializable
data class SubmissionFileInfo(
    val fileName: String,
    val url: String
)
