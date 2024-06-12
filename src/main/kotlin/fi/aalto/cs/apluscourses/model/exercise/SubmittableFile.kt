package fi.aalto.cs.apluscourses.model.exercise

import kotlinx.serialization.Serializable

@Serializable
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
data class SubmittableFile(val key: String, val name: String)
