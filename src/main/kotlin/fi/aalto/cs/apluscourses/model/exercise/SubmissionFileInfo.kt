package fi.aalto.cs.apluscourses.model.exercise

import kotlinx.serialization.Serializable

@Serializable
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
