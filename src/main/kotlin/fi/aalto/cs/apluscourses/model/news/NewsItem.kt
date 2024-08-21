package fi.aalto.cs.apluscourses.model.news

import fi.aalto.cs.apluscourses.MyBundle
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class NewsItem(
    val id: Long,
    val title: String,
    val body: String,
    private val publish: ZonedDateTime,
    var isRead: Boolean = false
) {
    val publishTimeInfo: String
        get() = MyBundle.message(
            "ui.toolWindow.subTab.news.publishTime",
            publish.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        )
}