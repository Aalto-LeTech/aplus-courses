package fi.aalto.cs.apluscourses.ui.news

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.junit5.TestApplication
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.ui.SettleScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.awt.Component
import java.awt.Container
import java.awt.event.ActionEvent
import java.time.ZonedDateTime
import javax.swing.*

@TestApplication
class NewsPanelTest {
    /** Collects what the panel wants to run once a resize settles, to run it on demand. */
    private class Settle : SettleScheduler {
        var pending: (() -> Unit)? = null
        var requests = 0

        override fun after(delayMillis: Long, action: () -> Unit) {
            pending = action
            requests++
        }

        fun run() {
            val action = requireNotNull(pending) { "nothing was scheduled" }
            pending = null
            action()
        }
    }

    /** A clock the test moves itself, so the throttle window never depends on real time. */
    private class Clock {
        var millis = 1000L
    }

    private fun item(id: Long, isRead: Boolean = false, body: String = SHORT_BODY) = NewsItem(
        id = id,
        title = "Announcement $id",
        body = body,
        publish = ZonedDateTime.parse(PUBLISHED),
        isRead = isRead
    )

    private fun contentOf(panel: NewsPanel): Component? = panel.components.singleOrNull()

    private fun componentsIn(root: Component): List<Component> {
        val found = mutableListOf<Component>()
        fun walk(component: Component) {
            found += component
            if (component is Container) component.components.forEach(::walk)
        }
        walk(root)
        return found
    }

    private fun chipsIn(root: Component): List<JButton> = componentsIn(root)
        .filterIsInstance<JButton>()
        .filter { it.icon == CoursesIcons.NewChip }

    private fun click(button: JButton) = button.doClick()

    /** Gives the panel a width the way the overview column does, and lays it out. */
    private fun NewsPanel.layOutAt(width: Int) {
        setBounds(0, 0, width, preferredSize.height)
        doLayout()
        setBounds(0, 0, width, preferredSize.height)
        doLayout()
    }

    @Test
    fun `news with nothing in them leave the section empty`(): Unit = runBlocking(Dispatchers.EDT) {
        val panel = NewsPanel(Settle()) {}

        panel.update(emptyList())

        assertNull(contentOf(panel), "an empty list has nothing to show")
    }

    @Test
    fun `an unread item is marked with a chip`(): Unit = runBlocking(Dispatchers.EDT) {
        val panel = NewsPanel(Settle()) {}

        panel.update(listOf(item(1), item(2, isRead = true)))

        assertNotNull(contentOf(panel))
        assertEquals(1, chipsIn(panel).size, "only the unread item is chipped")
    }

    @Test
    fun `a long announcement is wrapped to a taller section than a short one`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val short = NewsPanel(Settle()) {}
            short.update(listOf(item(1, body = SHORT_BODY)))
            short.layOutAt(WIDTH)

            val long = NewsPanel(Settle()) {}
            long.update(listOf(item(1, body = LONG_BODY)))
            long.layOutAt(WIDTH)

            assertTrue(
                long.preferredSize.height > short.preferredSize.height * 2,
                "the announcement has to be measured wrapped, not as one line: " +
                        "${long.preferredSize.height} against ${short.preferredSize.height}"
            )
        }

    @Test
    fun `a chip can be reached where it is drawn`(): Unit = runBlocking(Dispatchers.EDT) {
        val panel = NewsPanel(Settle()) {}
        panel.update(listOf(item(1)))
        val host = JPanel(null).apply { add(panel) }
        panel.layOutAt(WIDTH)
        host.setBounds(0, 0, WIDTH, panel.height)

        val chip = chipsIn(panel).single()
        val point = SwingUtilities.convertPoint(chip, chip.width / 2, chip.height / 2, host)

        assertTrue(
            chip.x + chip.width <= WIDTH,
            "a chip pushed out of the column cannot be clicked: ${chip.bounds}"
        )
        assertSame(
            chip,
            SwingUtilities.getDeepestComponentAt(host, point.x, point.y),
            "the chip has to be the component under its own middle"
        )
    }

    @Test
    fun `a resize inside the throttle window keeps the wrapping it has`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val settle = Settle()
            val clock = Clock()
            val panel = NewsPanel(settle, { clock.millis }) {}
            panel.update(listOf(item(1, body = LONG_BODY)))
            panel.layOutAt(WIDTH)
            val wrapped = panel.preferredSize

            clock.millis += 10
            panel.setBounds(0, 0, WIDTH * 3, wrapped.height)
            panel.doLayout()

            assertEquals(wrapped, panel.preferredSize, "a drag must not re-wrap on every event")
            assertEquals(1, settle.requests, "but the width it ended at has to be picked up later")
        }

    @Test
    fun `the settle pass wraps the news to the width the drag ended at`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val settle = Settle()
            val clock = Clock()
            val panel = NewsPanel(settle, { clock.millis }) {}
            panel.update(listOf(item(1, body = LONG_BODY)))
            panel.layOutAt(WIDTH)
            val narrow = panel.preferredSize
            clock.millis += 10
            panel.setBounds(0, 0, WIDTH * 3, narrow.height)
            panel.doLayout()

            settle.run()

            assertEquals(WIDTH * 3, panel.preferredSize.width, "the news now fill the column")
            assertTrue(
                panel.preferredSize.height < narrow.height,
                "wider wrapping is shorter: ${panel.preferredSize.height} against ${narrow.height}"
            )
        }

    @Test
    fun `a change of height alone costs nothing`(): Unit = runBlocking(Dispatchers.EDT) {
        val settle = Settle()
        val clock = Clock()
        val panel = NewsPanel(settle, { clock.millis }) {}
        panel.update(listOf(item(1, body = LONG_BODY)))
        panel.layOutAt(WIDTH)
        val wrapped = panel.preferredSize
        val requestsAfterWrapping = settle.requests

        clock.millis += 10
        panel.setBounds(0, 0, WIDTH, wrapped.height * 2)
        panel.doLayout()

        assertEquals(wrapped, panel.preferredSize)
        assertEquals(requestsAfterWrapping, settle.requests, "nothing to re-wrap, nothing to settle")
    }

    @Test
    fun `clicking a chip reports the news read`(): Unit = runBlocking(Dispatchers.EDT) {
        var marked = 0
        val panel = NewsPanel(Settle()) { marked++ }
        panel.update(listOf(item(1), item(2)))

        click(chipsIn(panel).first())

        assertEquals(1, marked)

        panel.update(listOf(item(1, isRead = true), item(2, isRead = true)))

        assertEquals(0, chipsIn(panel).size, "the chips go once the news are read")
    }

    @Test
    fun `the same news again keeps the section that is already built`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val panel = NewsPanel(Settle()) {}
            panel.update(listOf(item(1)))
            val content = contentOf(panel)
            assertNotNull(content)

            panel.update(listOf(item(1)))

            assertSame(content, contentOf(panel), "a rebuild here would flicker on every refresh")
        }

    @Test
    fun `an item turning read is rebuilt without its chip`(): Unit = runBlocking(Dispatchers.EDT) {
        val panel = NewsPanel(Settle()) {}
        panel.update(listOf(item(1)))
        val content = contentOf(panel)

        panel.update(listOf(item(1, isRead = true)))

        assertNotSame(content, contentOf(panel), "the chip has to go, so the section is rebuilt")
        assertEquals(0, chipsIn(panel).size)
    }

    @Test
    fun `read chips expose an accessible name and a keyboard action`(): Unit = runBlocking(Dispatchers.EDT) {
        var marked = 0
        val panel = NewsPanel(Settle()) { marked++ }
        panel.update(listOf(item(1)))
        val chip = chipsIn(panel).single()
        assertTrue(chip.isFocusable)
        assertTrue(chip.accessibleContext.accessibleName.isNotBlank())
        assertNotNull(chip.accessibleContext.accessibleAction)
        for (stroke in listOf("pressed SPACE", "released SPACE")) {
            val key = chip.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(stroke))
            val action = chip.actionMap.get(key)
            assertNotNull(action)
            action.actionPerformed(ActionEvent(chip, ActionEvent.ACTION_PERFORMED, stroke))
        }
        assertEquals(1, marked)
    }

    private companion object {
        @NonNls
        const val PUBLISHED = "2026-01-01T12:00:00Z"

        /** About the width the overview column has. */
        const val WIDTH = 236

        @NonNls
        const val SHORT_BODY = "Something happened."

        @NonNls
        const val LONG_BODY = "Something happened, and here is a rather long body so that the " +
                "wrapping width actually matters for how tall this announcement ends up being " +
                "on screen. Course announcements often run several paragraphs, and every one of " +
                "them has to be wrapped before its height is known."
    }
}
