package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.components.Service
import fi.aalto.cs.apluscourses.api.CourseConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NonNls
import org.yaml.snakeyaml.Yaml

@Service(Service.Level.APP)
class CoursesFetcher(private val cs: CoroutineScope) {
    data class CourseConfig(
        val name: String,
        val semester: String,
        val url: String,
        var config: CourseConfig.JSON? = null
    ) {
        companion object {
            @NonNls
            fun fromYaml(map: Map<String, String>): CourseConfig {
                return CourseConfig(map["name"]!!, map["semester"]!!, map["url"]!!)
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun fetchCourses(setCourses: (List<CoursesFetcher.CourseConfig>) -> Unit, updateCourses: () -> Unit) {
        cs.launch {
            val client = HttpClient(CIO)
//          val url = "https://version.aalto.fi/gitlab/aplus-courses/course-config-urls/-/raw/main/courses.yaml"
            val url = "https://raw.githubusercontent.com/jaakkonakaza/temp/main/courses.yaml" // TODO

            val res = client.get(url)

            val courses = Yaml()
                .load<List<Map<String, String>>>(res.bodyAsText())
                .map { course: Map<String, String> ->
                    CourseConfig.fromYaml(course)
                }

            setCourses(courses)

            for (course in courses) {
                val courseConfigRes = client.get(course.url)//client.getBody<CourseConfig.JSON>(course.url, false)
                val courseConfig = json.decodeFromString<CourseConfig.JSON>(courseConfigRes.bodyAsText())
                course.config = courseConfig
                updateCourses()
            }
            client.close()
        }
    }

    fun fetchCourse(url: String, setCourse: (CourseConfig?) -> Unit) {
        cs.launch {
            val client = HttpClient(CIO)
            try {
                val courseConfigRes = client.get(url)
                val courseConfig = json.decodeFromString<CourseConfig.JSON>(courseConfigRes.bodyAsText())
                setCourse(CourseConfig("", "", url, courseConfig))
            } catch (_: Exception) {
                setCourse(null)
            }
            client.close()

        }
    }
}