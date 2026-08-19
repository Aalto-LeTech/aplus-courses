package fi.aalto.cs.apluscourses.ui

import com.intellij.openapi.application.EDT
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.ui.components.BrowserLink
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import java.awt.Font
import javax.swing.JLabel
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import java.awt.Component
import java.awt.Container
import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.text.JTextComponent

@TestApplication
class SkeletonTest {

    @AfterEach
    fun stopTheShimmer(): Unit = ShimmerClock.reset()

    private fun componentsIn(root: Component): List<Component> {
        val found = mutableListOf<Component>()
        fun walk(component: Component) {
            found += component
            if (component is Container) component.components.forEach(::walk)
        }
        walk(root)
        return found
    }

    private fun textsIn(root: Component): List<String> = componentsIn(root).mapNotNull {
        when (it) {
            is JLabel -> it.text
            is JTextComponent -> it.text
            else -> null
        }
    }

    private fun property(value: String? = null) = AtomicProperty(value)

    @Test
    fun `a known value is rendered as text and an unknown one as a placeholder`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val known = panel { row { pendingText(property(COURSE_NAME), 100) } }
            val unknown = panel { row { pendingText(property(), 100) } }

            assertTrue(
                textsIn(known).any { it.contains(COURSE_NAME) },
                "expected the real text, got: ${textsIn(known)}"
            )
            assertTrue(componentsIn(known).none { it is SkeletonBlock }, "no placeholder when known")
            assertEquals(1, componentsIn(unknown).count { it is SkeletonBlock })
        }

    @Test
    fun `a value arriving replaces the placeholder in the cell already on screen`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val value = property()
            val panel = panel { row { pendingText(value, 100) } }
            val cell = componentsIn(panel).filterIsInstance<PendingValueLabel>().single()

            value.set(COURSE_NAME)

            assertSame(
                cell,
                componentsIn(panel).filterIsInstance<PendingValueLabel>().single(),
                "the cell itself should stay put"
            )
            assertTrue(
                textsIn(panel).any { it.contains(COURSE_NAME) },
                "expected the value, got: ${textsIn(panel)}"
            )
            assertEquals(0, componentsIn(panel).count { it is SkeletonBlock }, "placeholder should go")
        }

    @Test
    fun `a pending value shows its known parts and swaps the placeholder for the value`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val label = PendingValueLabel(placeholderWidth = 40, prefix = DOWNLOAD_SIZE_PREFIX)

            assertTrue(textsIn(label).any { it.contains(DOWNLOAD_SIZE_PREFIX.trim()) }, "prefix should be shown")
            assertEquals(1, componentsIn(label).count { it is SkeletonBlock })

            label.setValue(FILE_SIZE)

            assertEquals(0, componentsIn(label).count { it is SkeletonBlock }, "placeholder should go")
            assertTrue(
                textsIn(label).any { it.contains(FILE_SIZE) },
                "expected the value, got: ${textsIn(label)}"
            )
        }

    @Test
    fun `a link is a placeholder until its address is known`(): Unit = runBlocking(Dispatchers.EDT) {
        val link = PendingLink(placeholderWidth = 100, text = LINK_TEXT)

        assertEquals(1, componentsIn(link).count { it is SkeletonBlock })

        link.setUrl("https://plus.cs.aalto.fi/o1/")

        assertEquals(0, componentsIn(link).count { it is SkeletonBlock }, "placeholder should go")
        val browserLink = componentsIn(link).filterIsInstance<BrowserLink>().single()
        assertEquals("Course page", browserLink.text)
    }

    /**
     * A placeholder must be as tall as one line of the font it stands in for. A shorter
     * placeholder makes the loading rows more compact than the loaded ones.
     */
    @Test
    fun `a placeholder is as tall as the text it replaces`(): Unit = runBlocking(Dispatchers.EDT) {
        val value = property()
        val panel = panel { row { pendingText(value, 100) } }
        panel.setSize(300, 200)
        panel.doLayout()
        val cell = componentsIn(panel).filterIsInstance<PendingValueLabel>().single()
        val placeholderHeight = cell.preferredSize.height

        value.set(SHORT_VALUE)

        assertEquals(placeholderHeight, cell.preferredSize.height, "the row should not change height")
    }

    /** The shimmer timer must not keep running once a placeholder leaves the UI. */
    @Test
    fun `the shimmer only animates while the block is shown`(): Unit = runBlocking(Dispatchers.EDT) {
        val block = SkeletonBlock(100)
        assertFalse(block.isShimmering, "should not animate before being added")

        val parent = JPanel().apply { setSize(200, 50) }
        parent.add(block)
        parent.addNotify()
        assertTrue(block.isShimmering, "should animate once shown")

        parent.remove(block)
        assertFalse(block.isShimmering, "should stop animating once removed")
    }

    @Test
    fun `only the value and suffix become tabular, not the prefix`(): Unit = runBlocking(Dispatchers.EDT) {
        val label = PendingValueLabel(
            placeholderWidth = 40,
            prefix = DOWNLOAD_SIZE_PREFIX,
            tabularValue = true
        )
        label.setValue(FILE_SIZE)

        val fonts = componentsIn(label).filterIsInstance<JLabel>().map { it.font.family }
        assertTrue(fonts.contains(Font.MONOSPACED), "the number should be monospaced, got: $fonts")
        assertTrue(
            fonts.any { it != Font.MONOSPACED },
            "the prefix should stay in the UI font, got: $fonts"
        )
    }

    private fun render(block: SkeletonBlock): BufferedImage {
        block.setSize(120, 24)
        val image = BufferedImage(120, 24, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            block.paint(graphics)
        } finally {
            graphics.dispose()
        }
        return image
    }

    private fun sameImage(a: BufferedImage, b: BufferedImage): Boolean =
        a.width == b.width && a.height == b.height &&
                (0 until a.height).all { y -> (0 until a.width).all { x -> a.getRGB(x, y) == b.getRGB(x, y) } }

    /** Placeholders are replaced whenever the points table's category list changes. */
    @Test
    fun `a replacement placeholder continues the pulse instead of restarting it`(): Unit =
        runBlocking(Dispatchers.EDT) {
            ShimmerClock.reset()
            val parent = JPanel().apply { setSize(200, 50) }
            val original = SkeletonBlock(100)
            parent.add(original)
            parent.addNotify()

            val atRest = render(original)
            // About a quarter of the cycle's 150 frames.
            repeat(40) { ShimmerClock.tick() }
            val midPulse = render(original)
            assertFalse(
                sameImage(atRest, midPulse),
                "the pulse should have advanced, otherwise this test cannot tell a restart from a continuation"
            )

            parent.remove(original)
            val replacement = SkeletonBlock(100)
            parent.add(replacement)

            assertTrue(
                sameImage(midPulse, render(replacement)),
                "the replacement should pick the pulse up where the original left it"
            )
        }

    @Test
    fun `placeholders added at different times stay in step`(): Unit = runBlocking(Dispatchers.EDT) {
        ShimmerClock.reset()
        val parent = JPanel().apply { setSize(200, 50) }
        val early = SkeletonBlock(100)
        parent.add(early)
        parent.addNotify()

        repeat(7) { ShimmerClock.tick() }
        val late = SkeletonBlock(100)
        parent.add(late)
        repeat(3) { ShimmerClock.tick() }

        assertTrue(sameImage(render(early), render(late)), "both placeholders should be at the same phase")
    }

    private companion object {
        @NonNls
        const val COURSE_NAME = "Ohjelmointi 1"

        @NonNls
        const val DOWNLOAD_SIZE_PREFIX = "Download size "

        @NonNls
        const val FILE_SIZE = "1.23 MB"

        @NonNls
        const val LINK_TEXT = "Course page"

        @NonNls
        const val SHORT_VALUE = "A"
    }
}
