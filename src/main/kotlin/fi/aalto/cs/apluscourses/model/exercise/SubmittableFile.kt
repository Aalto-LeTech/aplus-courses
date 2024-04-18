package fi.aalto.cs.apluscourses.model.exercise

import kotlinx.serialization.Serializable

@Serializable
data class SubmittableFile(val key: String, val name: String)
