package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.Service
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NonNls
import org.yaml.snakeyaml.Yaml

@Service(Service.Level.APP)
class CoursesFetcher(private val cs: CoroutineScope) {
    data class CourseInfo(
        val name: String,
        val semester: String,
        val url: String,
        val language: String?
    ) {
        companion object {
            @NonNls
            fun fromYaml(map: Map<String, String>): CourseInfo {
                return CourseInfo(map["name"]!!, map["semester"]!!, map["url"]!!, map["language"])
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun client(): HttpClient = HttpClient(Java)

    /**
     * Fetches the list of known courses and hands the result to [onResult] on the EDT.
     */
    fun fetchCourses(onResult: (Result<List<CourseInfo>>) -> Unit) {
        cs.launch {
            val result = try {
                Result.success(
                    client().use { client ->
                        Yaml()
                            .load<List<Map<String, String>>>(client.get(COURSES_URL).bodyAsText())
                            .map { course: Map<String, String> -> CourseInfo.fromYaml(course) }
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CoursesLogger.error("Failed to fetch the course list", e)
                Result.failure(e)
            }

            withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) {
                onResult(result)
            }
        }
    }

    suspend fun fetchCourse(url: String): CourseConfig.JSON? {
        return try {
            client().use { client ->
                json.decodeFromString<CourseConfig.JSON>(client.get(url).bodyAsText())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CoursesLogger.error("Failed to fetch course config", e)
            null
        }
    }

    private companion object {
        @NonNls
        const val COURSES_URL =
            "https://version.aalto.fi/gitlab/aplus-courses/course-config-urls/-/raw/main/courses.yaml"
    }
}
