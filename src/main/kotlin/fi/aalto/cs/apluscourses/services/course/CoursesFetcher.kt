package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.components.Service
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    fun fetchCourses(setCourses: (List<CourseInfo>) -> Unit) {
        cs.launch {
            val client = HttpClient(CIO) {
                engine {
                    requestTimeout = 0
                }
            }
            val url = "https://version.aalto.fi/gitlab/aplus-courses/course-config-urls/-/raw/main/courses.yaml"
            val res = client.get(url)

            val courses = Yaml()
                .load<List<Map<String, String>>>(res.bodyAsText())
                .map { course: Map<String, String> ->
                    CourseInfo.fromYaml(course)
                }

            setCourses(courses)
            client.close()
        }
    }

    fun fetchCourse(url: String): CourseConfig.JSON? {
        return runBlocking(cs.coroutineContext) {
            try {
                val client = HttpClient(CIO) {
                    engine {
                        requestTimeout = 0
                    }
                }
                val res = client.get(url)
                client.close()
                json.decodeFromString<CourseConfig.JSON>(res.bodyAsText())
            } catch (e: Exception) {
                CoursesLogger.error("Failed to fetch course config", e)
                null
            }
        }
    }
}