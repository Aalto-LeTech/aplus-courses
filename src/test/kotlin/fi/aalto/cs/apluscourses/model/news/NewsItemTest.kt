package fi.aalto.cs.apluscourses.model.news

import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

@TestApplication
class NewsItemTest {
    @Test
    fun `the publish time is shown the way the system shows times`() {
        val item = NewsItem(
            id = 1,
            title = "Deadline moved",
            body = "The second round closes on Friday.",
            publish = ZonedDateTime.parse(PUBLISHED)
        )

        val shown = item.publishTimeInfo

        assertTrue(shown.isNotBlank())
        assertFalse(
            shown.contains(PUBLISHED),
            "the raw timestamp is not something to put in front of a student: $shown"
        )
    }

    @Test
    fun `missing publish times show no date text`() {
        assertEquals("", NewsItem(1, "Title", "Body", publish = null).publishTimeInfo)
    }

    private companion object {
        @NonNls
        const val PUBLISHED = "2026-01-01T12:00:00Z"
    }
}
