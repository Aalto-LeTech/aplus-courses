package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.NotificationType
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.aplus.client.AplusException
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import java.io.IOException

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

    private companion object {
        @NonNls
        const val SECOND_MODULE = "Otherwise"

        @NonNls
        const val API_URL = "https://plus.example.com/api/v2/courses/1/points/me"

        @NonNls
        const val BAD_URL = "not a url"

        @NonNls
        const val PLUGIN_NAME = "Scala"
    }
}
