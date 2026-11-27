package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.notification.impl.NotificationGroupEP
import com.intellij.openapi.application.EDT
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.services.course.CourseNews
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.ui.CLOSING_WIDTH
import fi.aalto.cs.apluscourses.ui.ShimmerClock
import fi.aalto.cs.apluscourses.ui.SkeletonBlock
import fi.aalto.cs.apluscourses.ui.news.NewsPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Point
import java.time.ZonedDateTime
import javax.swing.*
import kotlin.time.Instant

@TestApplication
class OverviewViewTest {
    private val projectFixture = projectFixture()

    private var replaceNotificationGroup: NotificationGroupEP? = null

    @BeforeEach
    fun isolateNotificationGroups() {
        // Replace's notification bundle cannot be resolved by the test plugin classloader.
        val ep = ExtensionPointName.create<NotificationGroupEP>("com.intellij.notificationGroup")
        replaceNotificationGroup = ep.extensionList.find { it.key == "notification.group.replace" }
        replaceNotificationGroup?.key = null
    }

    @AfterEach
    fun stopTheShimmer() {
        ShimmerClock.reset()
        replaceNotificationGroup?.key = "notification.group.replace"
    }

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

    @Test
    fun `an overview with no news keeps the section out of the way`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val project = projectFixture.get()
            CourseNews.getInstance(project).clear()

            val view = OverviewView(project)

            val news = componentsIn(view).filterIsInstance<NewsPanel>().single()
            assertFalse(isVisibleInTree(news), "an empty news section would leave a gap")
        }

    private fun newsItem(id: Long) = NewsItem(
        id = id,
        title = "Announcement $id",
        body = "Something happened",
        publish = ZonedDateTime.parse(PUBLISHED)
    )

    private fun chipsIn(root: Component) = componentsIn(root)
        .filterIsInstance<JButton>()
        .filter { it.icon == CoursesIcons.NewChip }

    @Test
    fun `news show up in the overview once they are fetched`(): Unit = runBlocking(Dispatchers.EDT) {
        val project = projectFixture.get()
        val view = OverviewView(project)
        CourseNews.getInstance(project).refreshed(listOf(newsItem(1)))

        view.update()

        val news = componentsIn(view).filterIsInstance<NewsPanel>().single()
        assertTrue(isVisibleInTree(news))
        assertEquals(1, chipsIn(news).size, "the unread item carries its chip into the overview")
    }

    /** Lays out the tree by hand, since a component with no window on it never validates. */
    private fun layOutTree(component: Component) {
        if (component is Container) {
            component.doLayout()
            component.components.forEach(::layOutTree)
        }
    }

    @Test
    fun `the news take the width of the overview column`(): Unit = runBlocking(Dispatchers.EDT) {
        val project = projectFixture.get()
        val view = OverviewView(project)
        CourseNews.getInstance(project).refreshed(listOf(newsItem(21)))
        view.update()
        val news = componentsIn(view).filterIsInstance<NewsPanel>().single()

        view.setSize(600, 400)
        layOutTree(view)
        val narrow = news.width

        view.setSize(900, 400)
        layOutTree(view)

        assertTrue(narrow > 0, "the news should be laid out at all")
        assertTrue(
            news.width > narrow,
            "the news reflow with the tool window like the rest of the overview " +
                    "(was $narrow, now ${news.width})"
        )
    }

    @Test
    fun `clicking a chip marks the news read and takes the chips away`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val project = projectFixture.get()
            val view = OverviewView(project)
            val news = CourseNews.getInstance(project)
            news.refreshed(listOf(newsItem(11), newsItem(12)))
            view.update()
            val chips = chipsIn(view)
            assertEquals(2, chips.size, "both items start unread")

            chips.first().doClick()
            view.update()

            assertTrue(news.current.all { it.isRead }, "one chip marks the whole feed read")
            assertEquals(0, chipsIn(view).size)
        }

    @Test
    fun `opening news returns to the top of a feed taller than the viewport`(): Unit = runBlocking(Dispatchers.EDT) {
        val project = projectFixture.get()
        CourseNews.getInstance(project).refreshed((1L..20L).map(::newsItem))
        val view = OverviewView(project)
        view.update()
        val news = componentsIn(view).filterIsInstance<NewsPanel>().single()
        val content = JPanel(null).apply {
            preferredSize = Dimension(500, 1500)
            add(news)
        }
        news.setBounds(0, 300, 500, 1000)
        val pane = JBScrollPane(content).apply {
            setSize(600, 200)
            doLayout()
        }
        val viewport = pane.viewport
        val top = SwingUtilities.convertPoint(news, 0, 0, viewport.view).y
        assertTrue(news.height > viewport.height)
        viewport.viewPosition = Point(0, top + 80)

        view.showNews()

        assertEquals(top, viewport.viewPosition.y)
    }

    @Test
    fun `news loaded on another tab gets height when the course tab opens`(): Unit = runBlocking(Dispatchers.EDT) {
        val project = projectFixture.get()
        val view = OverviewView(project)
        val tabs = JTabbedPane().apply {
            addTab("Course", view)
            addTab("Modules", JPanel())
            setSize(600, 400)
        }
        tabs.selectedIndex = 1
        CourseNews.getInstance(project).refreshed((1L..5L).map(::newsItem))
        view.update()
        val content = scrollPaneOf(view).viewport.view
        val beforeWrapping = content.preferredSize.height

        tabs.selectedIndex = 0
        layOutTree(tabs)
        UIUtil.dispatchAllInvocationEvents()
        repeat(2) { layOutTree(tabs) }

        val news = componentsIn(view).filterIsInstance<NewsPanel>().single()
        assertTrue(news.preferredSize.height > 0)
        assertTrue(
            content.preferredSize.height > beforeWrapping,
            "the overview must include the newly measured news height"
        )
        assertTrue(news.height > 0, "the news must receive layout space after returning to Course")
    }

    private companion object {
        @NonNls
        const val FAR_FUTURE = "2099-01-01T12:00:00Z"

        @NonNls
        const val PUBLISHED = "2026-01-01T12:00:00Z"
    }
}
