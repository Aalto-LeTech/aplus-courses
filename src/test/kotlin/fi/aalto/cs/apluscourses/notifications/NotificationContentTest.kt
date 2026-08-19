package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.NotificationType
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

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

    private companion object {
        @NonNls
        const val SECOND_MODULE = "Otherwise"

        @NonNls
        const val PLUGIN_NAME = "Scala"
    }
}
