package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.NotificationType
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.aplus.client.AplusException
import fi.aalto.cs.apluscourses.model.news.NewsItem
import io.ktor.http.*
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.ZonedDateTime

@TestApplication
class NotificationContentTest {
    private val projectFixture = projectFixture()

    @Test
    fun `missing dependency notification lists every unresolved module`() {
        val notification = MissingDependencyNotification(sortedSetOf("GoodStuff", SECOND_MODULE))

        assertTrue(notification.content.contains("GoodStuff"), notification.content)
        assertTrue(notification.content.contains(SECOND_MODULE), notification.content)
        assertEquals(NotificationType.ERROR, notification.type)
        assertTrue(notification.title.isNotBlank())
    }

    @Test
    fun `disabled plugins notification warns and offers an action`() {
        val notification = DisabledPluginsNotification(listOf(PLUGIN_NAME), projectFixture.get())

        assertTrue(notification.content.contains(PLUGIN_NAME), notification.content)
        assertEquals(NotificationType.WARNING, notification.type)
        assertEquals(1, notification.actions.size)
    }

    @Test
    fun `an API failure is described without the endpoint it happened on`() {
        val failure = AplusException.of(HttpStatusCode.Unauthorized, API_URL, "")

        val notification = NetworkErrorNotification(failure)

        assertFalse(notification.content.contains(API_URL), notification.content)
        assertFalse(notification.content.contains(failure.message!!), notification.content)
        assertTrue(notification.content.isNotBlank())
    }

    @Test
    fun `each API failure kind gets its own message`() {
        val messages = listOf(
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden,
            HttpStatusCode.NotFound,
            HttpStatusCode.InternalServerError
        ).map { userFacingMessage(AplusException.of(it, API_URL, "")) }

        assertEquals(messages.size, messages.toSet().size, "each status should read differently: $messages")
        assertTrue(messages.none { it.contains(API_URL) }, messages.toString())
    }

    @Test
    fun `a pagination failure is not described by its OK status`() {
        val failure = AplusException.Pagination(API_URL, "Pagination cycles: $API_URL was already followed")

        val message = userFacingMessage(failure)

        assertFalse(message.contains(API_URL), message)
        assertFalse(message.contains(HttpStatusCode.OK.value.toString()), message)
        assertNotEquals(
            userFacingMessage(AplusException.of(HttpStatusCode.InternalServerError, API_URL, "")),
            message,
            "a listing the plugin refused to walk is not a server error"
        )
    }

    @Test
    fun `a plain exception keeps its own message`() {
        assertEquals("no route to host", userFacingMessage(IOException("no route to host")))
    }

    @Test
    fun `an exception without a message falls back to its type`() {
        assertEquals("IOException", userFacingMessage(IOException()))
    }

    @Test
    fun `an invalid course URL notification names the address`() {
        val notification = InvalidCourseUrlNotification(BAD_URL)

        assertTrue(notification.content.contains(BAD_URL), notification.content)
        assertEquals(NotificationType.ERROR, notification.type)
    }

    private fun newsItem(@NonNls title: String, @NonNls body: String) = NewsItem(
        id = 1,
        title = title,
        body = body,
        publish = ZonedDateTime.parse(PUBLISHED)
    )

    @Test
    fun `the news notification shows the latest item rather than a number`() {
        val notification = CourseNewsNotification(newsItem(NEWS_TITLE, NEWS_BODY), unreadCount = 1)

        assertTrue(notification.content.contains(NEWS_TITLE), notification.content)
        assertTrue(
            notification.content.contains("The second round"),
            "the body is what says whether the news is worth opening: ${notification.content}"
        )
        assertFalse(notification.content.contains("<p>"), "and it is not shown as markup")
        assertEquals(NotificationType.INFORMATION, notification.type)
        assertEquals(1, notification.actions.size, "the balloon opens the news")
    }

    @Test
    fun `the news notification says how many others are waiting`() {
        val one = CourseNewsNotification(newsItem(NEWS_TITLE, NEWS_BODY), unreadCount = 1)
        val several = CourseNewsNotification(newsItem(NEWS_TITLE, NEWS_BODY), unreadCount = 3)

        assertFalse(one.content.contains("more"), "one item is the whole of it: ${one.content}")
        assertTrue(several.content.contains("2"), "the others are counted: ${several.content}")
    }

    @Test
    fun `a long body is cut at a word and marked as cut`() {
        val excerpt = excerptOf(LONG_BODY, limit = 40)

        assertTrue(excerpt.length <= 41, "a balloon has no room for the whole of it: $excerpt")
        assertTrue(excerpt.endsWith("…"), excerpt)
        assertFalse(
            excerpt.dropLast(1).endsWith(" "),
            "the cut lands after a word, not in the middle of one: $excerpt"
        )
        assertTrue(LONG_BODY.contains(excerpt.dropLast(1)), "and it is the body, unchanged")
    }

    @Test
    fun `markup and line breaks are taken out of an excerpt`() {
        val excerpt = excerptOf("<p>The deadline<br>moved to <b>Friday</b>.</p>")

        assertEquals("The deadline moved to Friday.", excerpt)
    }

    @Test
    fun `news notifications preserve literal angle brackets in HTML content`() {
        val notification = CourseNewsNotification(
            newsItem("List<String> & types", "<p>Use List&lt;String&gt; &amp; compare a &lt; b.</p>"),
            unreadCount = 1
        )
        assertTrue(notification.content.contains("<b>List&lt;String&gt; &amp; types</b>"))
        assertTrue(notification.content.contains("Use List&lt;String&gt; &amp; compare a &lt; b."))
        assertFalse(notification.content.contains("<String>"))
    }

    private companion object {
        @NonNls
        const val SECOND_MODULE = "Otherwise"

        @NonNls
        const val API_URL = "https://plus.example.com/api/v2/courses/1/points/me"

        @NonNls
        const val BAD_URL = "not a url"

        @NonNls
        const val NEWS_TITLE = "Deadline moved"

        @NonNls
        const val NEWS_BODY = "<p>The second round closes on Friday instead of Wednesday.</p>"

        @NonNls
        const val LONG_BODY = "The second round closes on Friday instead of Wednesday, " +
                "and the exercise session on Thursday is cancelled."

        @NonNls
        const val PUBLISHED = "2026-01-01T12:00:00Z"

        @NonNls
        const val PLUGIN_NAME = "Scala"
    }
}
