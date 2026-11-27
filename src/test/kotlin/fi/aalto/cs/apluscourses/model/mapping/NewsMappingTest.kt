package fi.aalto.cs.apluscourses.model.mapping

import fi.aalto.cs.aplus.client.dto.NewsItemDto
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class NewsMappingTest {
    @Test
    fun `an item in another language is dropped`() {
        val news = toNewsItems(
            listOf(newsItem(id = 1, language = FINNISH), newsItem(id = 2, language = ENGLISH)),
            courseLanguage = FINNISH,
            newsReadTimeEpochSeconds = 0
        )

        assertEquals(listOf(1L), news.map { it.id })
    }

    @Test
    fun `an item aimed at every language is kept whatever the course language is`() {
        val items = listOf(newsItem(id = 1, language = NewsItemDto.LANGUAGE_ALL))

        assertEquals(1, toNewsItems(items, FINNISH, 0).size)
        assertEquals(1, toNewsItems(items, ENGLISH, 0).size)
    }

    @Test
    fun `an item published before the read time is read`() {
        val news = toNewsItems(
            listOf(newsItem(publish = "2024-01-01T00:00:00Z")),
            courseLanguage = ENGLISH,
            newsReadTimeEpochSeconds = epochSecondsOf("2024-06-01T00:00:00Z")
        )

        assertTrue(news.single().isRead)
        assertEquals(0, news.count { !it.isRead })
    }

    @Test
    fun `an item published exactly at the read time is read`() {
        val publish = "2024-01-01T00:00:00Z"
        val news = toNewsItems(listOf(newsItem(publish = publish)), ENGLISH, epochSecondsOf(publish))

        assertTrue(news.single().isRead)
    }

    @Test
    fun `an item published after the read time is unread`() {
        val news = toNewsItems(
            listOf(newsItem(publish = "2024-06-01T00:00:00Z")),
            courseLanguage = ENGLISH,
            newsReadTimeEpochSeconds = epochSecondsOf("2024-01-01T00:00:00Z")
        )

        assertFalse(news.single().isRead)
        assertEquals(1, news.count { !it.isRead })
    }

    @Test
    fun `a never-read course leaves every item unread`() {
        val news = toNewsItems(
            listOf(newsItem(id = 1, publish = "2024-01-01T00:00:00Z"), newsItem(id = 2)),
            courseLanguage = ENGLISH,
            newsReadTimeEpochSeconds = 0
        )

        assertEquals(2, news.count { !it.isRead })
    }

    @Test
    fun `an item without a publish time is kept and counts as unread`() {
        val news = toNewsItems(listOf(newsItem(publish = null)), ENGLISH, 0)

        assertEquals(1, news.size)
        assertFalse(news.single().isRead, "an item nobody could have seen must not be hidden")
    }

    @Test
    fun `an unparseable publish time does not fail the whole feed and stays unread`() {
        val news = toNewsItems(
            listOf(newsItem(id = 1, publish = "not a timestamp"), newsItem(id = 2)),
            courseLanguage = ENGLISH,
            newsReadTimeEpochSeconds = epochSecondsOf("2030-01-01T00:00:00Z")
        )

        assertEquals(listOf(1L, 2L), news.map { it.id })
        assertFalse(news.first().isRead, "an undatable item cannot be older than the read cut-off")
        assertTrue(news.last().isRead)
    }

    @Test
    fun `an item with no date shows no published-on line`() {
        val news = toNewsItems(listOf(newsItem(publish = null)), ENGLISH, 0)

        assertEquals("", news.single().publishTimeInfo)
    }

    @Test
    fun `the title and body come through untouched`() {
        val news = toNewsItems(listOf(newsItem(title = "Week 1", body = "<p>Hello</p>")), ENGLISH, 0)

        val item = news.single()
        assertEquals("Week 1", item.title)
        assertEquals("<p>Hello</p>", item.body)
    }

    @Test
    fun `an empty feed maps to an empty list`() {
        assertTrue(toNewsItems(emptyList(), ENGLISH, 0).isEmpty())
    }

    private fun newsItem(
        id: Long = 1,
        title: String = "Title",
        body: String = "Body",
        publish: String? = "2024-06-01T00:00:00Z",
        language: String = NewsItemDto.LANGUAGE_ALL
    ) = NewsItemDto(id = id, title = title, body = body, publish = publish, language = language)

    private fun epochSecondsOf(@NonNls timestamp: String): Long = ZonedDateTime.parse(timestamp).toEpochSecond()

    companion object {
        @NonNls
        private const val FINNISH = "fi"

        @NonNls
        private const val ENGLISH = "en"
    }
}
