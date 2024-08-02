package fi.aalto.cs.apluscourses.model.temp

import java.lang.Exception

class MalformedCourseConfigurationException(
    pathToConfigurationFile: String,
    message: String,
    cause: Throwable?
) : Exception(message, cause) {
    private val pathToConfigurationFile: String

    init {
        this.pathToConfigurationFile = pathToConfigurationFile
    }

    fun getPathToConfigurationFile(): String {
        return pathToConfigurationFile
    }
}
