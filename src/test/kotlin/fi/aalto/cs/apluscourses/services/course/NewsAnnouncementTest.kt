package fi.aalto.cs.apluscourses.services.course

import fi.aalto.cs.apluscourses.model.news.NewsItem
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class NewsAnnouncementTest {
    private fun item(id: Long, isRead: Boolean = false) = NewsItem(
        id = id,
        title = "Announcement $id",
        body = "Something happened",
        publish = ZonedDateTime.parse(PUBLISHED),
        isRead = isRead
    )

    @Test
    fun `the first fetch of a session sums up what is unread`() {
        val fresh = newsWorthAnnouncing(
            listOf(item(1), item(2), item(3, isRead = true)),
            announced = emptySet()
        )

        assertEquals(listOf(1L, 2L), fresh.map { it.id }, "one balloon covers the unread ones")
    }

    @Test
    fun `a refresh that brings nothing new says nothing`() {
        val fresh = newsWorthAnnouncing(
            listOf(item(1), item(2)),
            announced = setOf(1L, 2L)
        )

        assertTrue(fresh.isEmpty(), "still unread is not the same as new")
    }

    @Test
    fun `a refresh announces only what was not in the feed before`() {
        val fresh = newsWorthAnnouncing(
            listOf(item(1), item(2), item(3)),
            announced = setOf(1L, 2L)
        )

        assertEquals(listOf(3L), fresh.map { it.id })
    }

    private companion object {
        @NonNls
        const val PUBLISHED = "2026-01-01T12:00:00Z"
    }
}
