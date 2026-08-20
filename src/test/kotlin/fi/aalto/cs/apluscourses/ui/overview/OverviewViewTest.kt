package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.ui.CLOSING_WIDTH
import fi.aalto.cs.apluscourses.ui.ShimmerClock
import fi.aalto.cs.apluscourses.ui.SkeletonBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import java.awt.Component
import java.awt.Container
import java.awt.Point
import javax.swing.ScrollPaneConstants
import javax.swing.Scrollable
import kotlin.time.Instant

@TestApplication
class OverviewViewTest {
    private val projectFixture = projectFixture()

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

    private fun scrollPaneOf(view: OverviewView) =
        componentsIn(view).filterIsInstance<JBScrollPane>().single()

    @Test
    fun `an update keeps the banner it already has`(): Unit = runBlocking(Dispatchers.EDT) {
        val view = OverviewView(projectFixture.get())
        val banner = componentsIn(view).filterIsInstance<ResponsiveImagePanel>().single()

        view.update()

        assertSame(
            banner,
            componentsIn(view).filterIsInstance<ResponsiveImagePanel>().single(),
            "a new banner would refetch the image and flash its placeholder back up"
        )
    }

    @Test
    fun `an update keeps the points tables it already has`(): Unit = runBlocking(Dispatchers.EDT) {
        val view = OverviewView(projectFixture.get())
        val tables = componentsIn(view).filterIsInstance<PointsTable>()
        assertEquals(2, tables.size, "the collected table and the next-grade one")

        view.update()

        assertEquals(tables, componentsIn(view).filterIsInstance<PointsTable>())
    }

    @Test
    fun `an update does not replace the scroll pane or what it shows`(): Unit = runBlocking(Dispatchers.EDT) {
        val view = OverviewView(projectFixture.get())
        val scrollPane = scrollPaneOf(view)
        val shown = scrollPane.viewport.view

        view.update()

        val current = scrollPaneOf(view)
        assertSame(scrollPane, current, "a new scroll pane is what used to reset the scroll position")
        assertSame(shown, current.viewport.view, "and so is a new view inside it")
    }

    @Test
    fun `an update leaves the scroll position alone`(): Unit = runBlocking(Dispatchers.EDT) {
        val view = OverviewView(projectFixture.get())
        val scrollPane = scrollPaneOf(view)
        scrollPane.setSize(300, 120)
        scrollPane.doLayout()

        val contentHeight = scrollPane.viewport.view.preferredSize.height
        assertTrue(
            contentHeight > 180,
            "the overview must be taller than the viewport or this test proves nothing (was $contentHeight)"
        )
        scrollPane.viewport.viewPosition = Point(0, 60)

        view.update()

        val current = scrollPaneOf(view)
        assertSame(scrollPane, current, "a replaced scroll pane would take the position with it")
        assertEquals(Point(0, 60), current.viewport.viewPosition)
    }

    @Test
    fun `a narrow tool window scrolls the main screen instead of squeezing it`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val view = OverviewView(projectFixture.get())
            val scrollPane = scrollPaneOf(view)
            scrollPane.setSize(120, 400)
            scrollPane.doLayout()

            assertEquals(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                scrollPane.horizontalScrollBarPolicy
            )
            assertFalse(
                (scrollPane.viewport.view as Scrollable).scrollableTracksViewportWidth,
                "the main screen keeps its preferred width and scrolls sideways"
            )
        }

    @Test
    fun `the deadline line is reserved while the rounds are still loading`() {
        val loading = OverviewModel(Screen.MAIN)

        assertTrue(loading.showsClosingWeek(), "nothing has loaded yet, so the line is held open")
    }

    @Test
    fun `the deadline line is dropped once the rounds are known and none is open`() {
        val loaded = OverviewModel(Screen.MAIN, weeksKnown = true)

        assertFalse(
            loaded.showsClosingWeek(),
            "there is nothing left to wait for, so a placeholder here would never resolve"
        )
    }

    @Test
    fun `a course that is over has no deadline at all`() {
        val ended = OverviewModel(Screen.MAIN, courseHasEnded = true)

        assertFalse(ended.showsClosingWeek())
    }

    @Test
    fun `a known deadline is shown`() {
        val open = OverviewModel(
            Screen.MAIN,
            weeksKnown = true,
            closingWeek = ClosingWeek(1, Instant.parse(FAR_FUTURE))
        )

        assertTrue(open.showsClosingWeek())
    }

    @Test
    fun `a project with nothing loaded holds the deadline line open`(): Unit = runBlocking(Dispatchers.EDT) {
        val model = readOverviewModel(projectFixture.get())

        assertFalse(model.weeksKnown, "the fixture has no rounds")
        assertTrue(model.showsClosingWeek())
    }

    @Test
    fun `a course with no categories gets one total row of the course totals`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val project = projectFixture.get()
            val state = ExercisesUpdater.getInstance(project).state
            state.userPointsForCategories = emptyMap()
            state.maxPointsForCategories = emptyMap()
            state.userPointsTotal = 15
            state.maxPointsTotal = 300

            val model = readOverviewModel(project)

            val total = model.categories?.singleOrNull()
            assertTrue(total != null, "expected exactly one synthetic category, got: ${model.categories}")
            assertEquals(15, model.collected?.get(total))
            assertEquals(300, model.maxPoints?.get(total))
        }

    private fun isVisibleInTree(component: Component): Boolean =
        generateSequence(component) { it.parent }.all { it.isVisible }

    @Test
    fun `a fresh overview shows a placeholder where the deadline will go`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val view = OverviewView(projectFixture.get())

            val placeholder = componentsIn(view)
                .filterIsInstance<SkeletonBlock>()
                .filter { it.preferredSize.width == JBUI.scale(CLOSING_WIDTH) }

            assertEquals(1, placeholder.size, "expected the deadline placeholder")
            assertTrue(isVisibleInTree(placeholder.single()), "and its row should be showing")
        }

    private companion object {
        @NonNls
        const val FAR_FUTURE = "2099-01-01T12:00:00Z"
    }
}
