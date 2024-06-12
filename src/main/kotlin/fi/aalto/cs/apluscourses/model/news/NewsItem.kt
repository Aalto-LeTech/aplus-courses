package fi.aalto.cs.apluscourses.model.news

import fi.aalto.cs.apluscourses.intellij.utils.Interfaces.ReadNews
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces.ReadNewsImpl
import fi.aalto.cs.apluscourses.model.Browsable
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
import fi.aalto.cs.apluscourses.utils.parser.NewsParser
import fi.aalto.cs.apluscourses.utils.parser.O1NewsParser
import org.json.JSONObject
import org.jsoup.Jsoup
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class NewsItem(
    val id: Long,
    private val url: String,
    val title: String,
    val body: String,
    private val publish: ZonedDateTime,
    private val readNews: ReadNews
) : Browsable {
    override fun getHtmlUrl(): String {
        return url
    }

    val publishTimeInfo: String
        get() = PluginResourceBundle.getAndReplaceText(
            "ui.toolWindow.subTab.news.publishTime",
            publish.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        )

    val isRead: Boolean
        /**
         * Returns true if the news is read.
         */
        get() {
            val readNewsString = readNews.readNews
            return (readNewsString != null
                    && Arrays.stream(readNewsString.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .anyMatch { idS: String -> idS.toLong() == this.id })
        }

    fun setRead() {
        readNews.setNewsRead(id)
    }

    companion object {
        /**
         * Constructs News from JSON.
         */
        @JvmStatic
        fun fromJsonObject(`object`: JSONObject, course: Course, language: String): NewsItem {
            val id = `object`.getLong("id")
            val url = course.htmlUrl

            val title = `object`.getString("title")
            val titleElement = Jsoup.parseBodyFragment(title).body()

            val body = `object`.getString("body")
            val bodyElement = Jsoup.parseBodyFragment(body).body()

            val parser: NewsParser
            val parserKey = Optional.ofNullable(course.newsParser).orElse(course.name)
            parser = when (parserKey) {
                O1NewsParser.NAME -> O1NewsParser(language)
                else -> NewsParser()
            }
            val titleText = parser.parseTitle(titleElement)
            val bodyText = parser.parseBody(bodyElement)

            val publishString = `object`.getString("publish")
            val publish = ZonedDateTime.parse(publishString)
            return NewsItem(id, url, titleText, bodyText, publish, ReadNewsImpl())
        }
    }
}