package fi.aalto.cs.apluscourses.model.exercise

import kotlinx.serialization.Serializable

@Serializable
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
data class SubmissionFileInfo(
    val fileName: String,
    val url: String
) {
//    companion object {
//        fun fromJsonObject(jsonObject: JsonObject): SubmissionFileInfo {
//            return SubmissionFileInfo(jsonObject["filename"].to(String), jsonObject.getString("url"))
//        }
//    }
}
