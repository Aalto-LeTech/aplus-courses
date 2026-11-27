package fi.aalto.cs.apluscourses.api

import com.intellij.openapi.project.Project
import fi.aalto.cs.aplus.courseconfig.CourseConfig
import fi.aalto.cs.apluscourses.notifications.CourseConfigurationError
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.aplus.courseconfig.ConfigVersion
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

object CourseConfigLoader {
    suspend fun get(project: Project): CourseConfig.Document? {
        val url = CourseFileManager.getInstance(project).state.url ?: return null
        try {
            return deserialize(CoursesClient.getInstance(project).get(url).bodyAsText())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            when (error) {
                is IOException, is UnresolvedAddressException -> {
                    CoursesLogger.warn("Network error loading course configuration", error)
                    throw error
                }

                else -> {
                    CoursesLogger.error("Invalid course configuration", error)
                    Notifier.notify(CourseConfigurationError(error), project)
                    return null
                }
            }
        }
    }

    fun deserialize(json: String): CourseConfig.Document = CourseConfig.parse(json).also { config ->
        requireNotNull(config.aPlusUrl) { "aPlusUrl is required by A+ Courses" }
        require(config.languages.isNotEmpty()) { "At least one course language is required by A+ Courses" }
        ConfigVersion.parse(config.version)
        config.modules.forEach { ConfigVersion.parse(it.version) }
    }

    fun resourceUrls(resources: CourseConfig.Resources?): Map<String, Url> {
        if (resources == null) return emptyMap()
        return mapOf(
            "ideSettings" to resources.ideSettings,
            "ideSettingsMac" to resources.ideSettingsMac,
            "ideSettingsLinux" to resources.ideSettingsLinux,
            "ideSettingsWindows" to resources.ideSettingsWindows,
            "projectSettings" to resources.projectSettings,
            "customProperties" to resources.customProperties,
            "feedbackCss" to resources.feedbackCss,
        ).mapNotNull { (key, value) -> value?.let { key to Url(it) } }.toMap()
    }
}
