package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.junit5.TestApplication
import fi.aalto.cs.apluscourses.ui.PLACEHOLDER_POINTS_ROWS
import fi.aalto.cs.apluscourses.ui.ShimmerClock
import fi.aalto.cs.apluscourses.ui.SkeletonBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import java.awt.Component
import java.awt.Container
import javax.swing.JLabel
import javax.swing.JProgressBar

@TestApplication
class PointsTableTest {

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

    private fun barsIn(table: PointsTable) = componentsIn(table).filterIsInstance<JProgressBar>()

    private fun textOf(table: PointsTable) =
        componentsIn(table).filterIsInstance<JLabel>().joinToString("") { it.text }

    private fun labelsOf(table: PointsTable) =
        componentsIn(table).filterIsInstance<JLabel>().map { it.text }

    @Test
    fun `without categories it reserves a fixed number of rows`(): Unit = runBlocking(Dispatchers.EDT) {
        val table = PointsTable()

        assertEquals(PLACEHOLDER_POINTS_ROWS, barsIn(table).size)
        assertTrue(barsIn(table).all { it.isIndeterminate }, "nothing is known, so no bar has a value")
    }

    @Test
    fun `a known-empty category list shows no rows instead of placeholders`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val table = PointsTable()

            table.update(emptyList(), emptyMap())

            assertEquals(0, barsIn(table).size, "there is nothing to load, so nothing to hold a row for")
            assertEquals(0, componentsIn(table).count { it is SkeletonBlock })
        }

    @Test
    fun `a fully known row uses a determinate bar and shows the score`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val table = PointsTable()

            table.update(listOf(A), mapOf(A to Points.Collected(collected = 10, max = 2200)))

            val bar = barsIn(table).single()
            assertFalse(bar.isIndeterminate)
            assertEquals(10, bar.value)
            assertEquals(2200, bar.maximum)
            assertTrue(textOf(table).contains("10/2200"), "expected the score, got: ${textOf(table)}")
            assertEquals(0, componentsIn(table).count { it is SkeletonBlock })
        }

    @Test
    fun `the remaining label counts down instead of showing a fraction`(): Unit = runBlocking(Dispatchers.EDT) {
        val table = PointsTable()

        table.update(
            listOf(A, B),
            mapOf(
                A to Points.Remaining(untilNext = 150, maxOfNext = 300),
                B to Points.Remaining(untilNext = 0, maxOfNext = 300),
            )
        )

        val labels = labelsOf(table)
        assertTrue(labels.contains("150"), "expected the points still needed, got: $labels")
        assertTrue(labels.contains("0"), "a finished category should read 0 on its own row, got: $labels")
        assertFalse(labels.any { it.contains("/") }, "the countdown is a single number, got: $labels")
    }

    @Test
    fun `both tables leave their bars the same length`(): Unit = runBlocking(Dispatchers.EDT) {
        val collected = PointsTable()
        collected.update(
            listOf(A, B),
            mapOf(
                A to Points.Collected(collected = 120, max = 2200),
                B to Points.Collected(collected = 40, max = 1100),
            )
        )
        val untilNext = PointsTable()
        untilNext.update(
            listOf(A, B),
            mapOf(
                A to Points.Remaining(untilNext = 150, maxOfNext = 300),
                B to Points.Remaining(untilNext = 0, maxOfNext = 300),
            )
        )

        val scoreWidth = maxOf(collected.naturalScoreWidth(), untilNext.naturalScoreWidth())
        collected.pinScoreWidth(scoreWidth)
        untilNext.pinScoreWidth(scoreWidth)

        listOf(collected, untilNext).forEach { it.setSize(400, 200); it.doLayout() }

        val collectedBars = barsIn(collected).map { it.width }
        val untilNextBars = barsIn(untilNext).map { it.width }
        assertEquals(
            collectedBars,
            untilNextBars,
            "bars should be the same length in both tables"
        )
    }

    @Test
    fun `the maximum is shown before the score that goes with it`(): Unit = runBlocking(Dispatchers.EDT) {
        val table = PointsTable()

        table.update(listOf(A), mapOf(A to Points.Collected(collected = null, max = 2200)))

        assertTrue(barsIn(table).single().isIndeterminate, "the score is unknown")
        assertTrue(textOf(table).contains("/2200"), "expected the maximum, got: ${textOf(table)}")
        assertEquals(
            1,
            componentsIn(table).count { it is SkeletonBlock },
            "expected exactly the unknown score to be a placeholder"
        )
    }

    @Test
    fun `it writes new points into the rows it already has`(): Unit = runBlocking(Dispatchers.EDT) {
        val table = PointsTable()
        table.update(listOf(A, B), mapOf(A to Points.Collected(1, 10), B to Points.Collected(2, 10)))
        val bars = barsIn(table)

        table.update(listOf(A, B), mapOf(A to Points.Collected(5, 10), B to Points.Collected(6, 10)))

        assertEquals(bars, barsIn(table), "the rows should be the same components")
        assertEquals(5, bars[0].value, "and should have been updated in place")
        assertEquals(6, bars[1].value)
    }

    @Test
    fun `it rebuilds its rows when the category list changes`(): Unit = runBlocking(Dispatchers.EDT) {
        val table = PointsTable()
        table.update(listOf(A, B), mapOf(A to Points.Collected(1, 10), B to Points.Collected(2, 10)))
        val bars = barsIn(table)

        table.update(listOf(A, B, C), mapOf(A to Points.Collected(1, 10)))

        assertEquals(3, barsIn(table).size, "the new category needs a row")
        assertNotEquals(bars, barsIn(table))
    }

    @Test
    fun `categories arriving names the rows`(): Unit = runBlocking(Dispatchers.EDT) {
        val table = PointsTable()
        assertEquals(PLACEHOLDER_POINTS_ROWS, barsIn(table).size)

        table.update(listOf(A, B), emptyMap())

        assertEquals(2, barsIn(table).size)
        assertTrue(textOf(table).contains(A) && textOf(table).contains(B))
    }

    private companion object {
        @NonNls
        const val A = "A"

        @NonNls
        const val B = "B"

        @NonNls
        const val C = "C"
    }
}
