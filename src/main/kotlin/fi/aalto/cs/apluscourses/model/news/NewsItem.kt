package fi.aalto.cs.apluscourses.model.news

import com.intellij.util.text.DateFormatUtil
import fi.aalto.cs.apluscourses.MyBundle
import java.time.ZonedDateTime

data class NewsItem(
    val id: Long,
    val title: String,
    val body: String,
    /** Null when A+ sent no publish time, or one that could not be parsed. */
    val publish: ZonedDateTime?,
    val isRead: Boolean = false
) {
    /** Uses the IDE date format. Missing dates produce no text. */
    val publishTimeInfo: String
        get() = publish?.let {
            MyBundle.message(
                "ui.NewsPanel.publishTime",
                DateFormatUtil.formatDateTime(it.toInstant().toEpochMilli())
            )
        } ?: ""
}
