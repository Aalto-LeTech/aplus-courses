package fi.aalto.cs.apluscourses.model.mapping

import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.model.news.NewsList
import fi.aalto.cs.aplus.client.dto.NewsItemDto
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

/**
 * Maps the A+ news feed to the course language and read cutoff.
 *
 * [NewsItemDto.LANGUAGE_ALL] includes an item in every language.
 * Items published at or before [newsReadTimeEpochSeconds] count as read.
 */
fun toNewsList(
    items: List<NewsItemDto>,
    courseLanguage: String,
    newsReadTimeEpochSeconds: Long
): NewsList = NewsList(
    items
        .filter { it.appliesTo(courseLanguage) }
        .map { item ->
            val publish = parsePublishTime(item.id, item.publish)
            NewsItem(
                item.id,
                item.title,
                item.body,
                publish,
                // Missing dates leave announcements unread so students still see them.
                publish != null && newsReadTimeEpochSeconds >= publish.toEpochSecond()
            )
        }
)

private fun parsePublishTime(id: Long, publish: String?): ZonedDateTime? {
    if (publish == null) {
        CoursesLogger.warn("News item $id has no publish time")
        return null
    }
    return try {
        ZonedDateTime.parse(publish)
    } catch (e: DateTimeParseException) {
        CoursesLogger.warn("News item $id has an unparseable publish time \"$publish\"", e)
        null
    }
}
