package fi.aalto.cs.apluscourses.model.news

import fi.aalto.cs.apluscourses.MyBundle
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class NewsItem(
    val id: Long,
    val title: String,
    val body: String,
    /** Null when A+ sent no publish time, or one that could not be parsed. */
    private val publish: ZonedDateTime?,
    var isRead: Boolean = false
) {
    /** The publish date text is empty when A+ supplies no usable date. */
    val publishTimeInfo: String
        get() = publish?.let {
            MyBundle.message(
                "ui.NewsView.publishTime",
                it.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            )
        } ?: ""
}