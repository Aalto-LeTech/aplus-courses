package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.components.Service
import fi.aalto.cs.apluscourses.api.CourseConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NonNls
import org.yaml.snakeyaml.Yaml

@Service(Service.Level.APP)
class CoursesFetcher(private val cs: CoroutineScope) {
    data class CourseConfig(
        val name: String,
        val semester: String,
        val url: String,
        val language: String?
    ) {
        companion object {
            @NonNls
            fun fromYaml(map: Map<String, String>): CourseConfig {
                return CourseConfig(map["name"]!!, map["semester"]!!, map["url"]!!, map["language"])
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun fetchCourses(setCourses: (List<CoursesFetcher.CourseConfig>) -> Unit) {
        cs.launch {
            val client = HttpClient(CIO)
            val url = "https://version.aalto.fi/gitlab/aplus-courses/course-config-urls/-/raw/main/courses.yaml"
            val res = client.get(url)

            val courses = Yaml()
                .load<List<Map<String, String>>>(res.bodyAsText())
                .map { course: Map<String, String> ->
                    CourseConfig.fromYaml(course)
                }

            setCourses(courses)
            client.close()
        }
    }

    fun fetchCourse(url: String): CourseConfig.JSON? {
        return runBlocking(cs.coroutineContext) {
            try {
                val client = HttpClient(CIO)
                val res = client.get(url)
                client.close()
                json.decodeFromString<CourseConfig.JSON>(res.bodyAsText())
            } catch (_: Exception) {
                null
            }
        }
    }
}