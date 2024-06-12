package fi.aalto.cs.apluscourses.api

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces.ReadNewsImpl
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.model.news.NewsTree
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.utils.parser.NewsParser
import fi.aalto.cs.apluscourses.utils.parser.O1NewsParser
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import java.time.ZonedDateTime
import java.util.*

@Resource("/courses/{id}")
//@Suppress("PROVIDED_RUNTIME_TOO_LOW") // TODO: https://github.com/awslabs/aws-glue-schema-registry/issues/313
class Course(val id: Int) {
    @Resource("news")
    class News(val parent: Course) {
        suspend fun get(project: Project): NewsTree {
            println("news get")
            val resource = this
            val res = withContext(Dispatchers.IO) {
                project.service<CoursesClient>().getBody<News, Body>(resource)
            }
            println("news res ${res}")

            return NewsTree(res.results.map { (id, url, title, _, publishString, language, body, pin) ->
                val titleElement = Jsoup.parseBodyFragment(title).body()

                val bodyElement = Jsoup.parseBodyFragment(body).body()

                val parser: NewsParser = NewsParser()
//                val parserKey = null//Optional.ofNullable(course.newsParser).orElse(course.name)
//                parser = when (parserKey) {
//                    O1NewsParser.NAME -> O1NewsParser(language)
//                    else -> NewsParser()
//                }
                val titleText = parser.parseTitle(titleElement)
                val bodyText = parser.parseBody(bodyElement)
                println(titleText)

                val publish = ZonedDateTime.parse(publishString)
                NewsItem(
                    id,
                    url,
                    titleText,
                    bodyText,
                    publish,
                    ReadNewsImpl()
                )
            })
        }

        @Serializable
        data class Body(
            val results: List<NewsItemRes>,
        )

        @Serializable
        data class NewsItemRes(
            val id: Long,
            val url: String,
            val title: String,
            val audience: Long,
            val publish: String,
            val language: String,
            val body: String,
            val pin: Boolean,
        )
    }

    fun news(): News = News(this)
}

object APlusApi