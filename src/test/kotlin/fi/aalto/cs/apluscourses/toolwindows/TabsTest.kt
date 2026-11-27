package fi.aalto.cs.apluscourses.toolwindows

import com.intellij.openapi.wm.ToolWindow
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.swing.JPanel

@TestApplication
class TabsTest {
    private fun tab(@NonNls name: String): Content =
        ContentFactory.getInstance().createContent(JPanel(), name, true)

    @Test
    fun `unread course news keeps its indicator and tooltip in both tab modes`() {
        val overview = tab(OVERVIEW)
        val tabs = listOf(overview to OVERVIEW)
        updateCourseTabNews(overview, unread = true)

        for (show in listOf(true, false, true)) {
            showTabTitles(tabs, show)
            assertSame(if (show) CoursesIcons.UnreadNews else CoursesIcons.LogoUnread, overview.icon)
            assertTrue(overview.getUserData(ToolWindow.SHOW_CONTENT_ICON) == true)
            assertTrue(overview.description.orEmpty().contains("Unread course news"))
            assertEquals(OVERVIEW, overview.displayName)
        }

        updateCourseTabNews(overview, unread = false)
        assertSame(CoursesIcons.LogoColor, overview.icon)
        assertNull(overview.description)
        assertFalse(overview.getUserData(ToolWindow.SHOW_CONTENT_ICON) == true)
        showTabTitles(tabs, show = true)
        assertFalse(overview.getUserData(ToolWindow.SHOW_CONTENT_ICON) == true)
        showTabTitles(tabs, show = false)
        assertEquals(OVERVIEW, overview.description)
        updateCourseTabNews(overview, unread = true)
        updateCourseTabNews(overview, unread = false)
        assertEquals(OVERVIEW, overview.description)
    }

    private fun selectionEvent(content: Content, operation: ContentManagerEvent.ContentOperation) =
        ContentManagerEvent(this, content, 0, operation)

    @Test
    fun `leaving the overview is what marks the news read`() {
        val overview = tab(OVERVIEW)

        assertTrue(
            leavesOverview(
                selectionEvent(overview, ContentManagerEvent.ContentOperation.remove),
                overview
            )
        )
    }

    @Test
    fun `arriving at the overview or leaving another tab does not`() {
        val overview = tab(OVERVIEW)
        val other = tab(ASSIGNMENTS)

        assertFalse(
            leavesOverview(
                selectionEvent(overview, ContentManagerEvent.ContentOperation.add),
                overview
            ),
            "a switch reports both tabs, and acting on both would mark the news read twice"
        )
        assertFalse(
            leavesOverview(
                selectionEvent(other, ContentManagerEvent.ContentOperation.remove),
                overview
            )
        )
    }

    /** The two tabs and the list the switch sets them through. */
    private fun twoTabs(): Triple<List<Pair<Content, String>>, Content, Content> {
        val overview = tab(OVERVIEW)
        val assignments = tab(ASSIGNMENTS)
        return Triple(listOf(overview to OVERVIEW, assignments to ASSIGNMENTS), overview, assignments)
    }

    private fun isIcon(tab: Content): Boolean =
        tab.getUserData(ToolWindow.SHOW_CONTENT_ICON) == true &&
                tab.getUserData(ToolWindow.SHOW_CONTENT_TAB_LABEL_TEXT) == false

    @Test
    fun `a tab down to its icon keeps the name the dropdown shows`() {
        val (tabs, overview, assignments) = twoTabs()

        showTabTitles(tabs, show = false)

        assertTrue(isIcon(overview) && isIcon(assignments))
        assertEquals(
            listOf(OVERVIEW, ASSIGNMENTS),
            tabs.map { it.first.displayName },
            "tabs that do not fit overflow into a dropdown that has only the name to render"
        )
        assertEquals(
            listOf(OVERVIEW, ASSIGNMENTS),
            tabs.map { it.first.description },
            "and an icon says what it is on hover"
        )
    }

    @Test
    fun `a tab with room for its title needs no tooltip`() {
        val (tabs, overview, assignments) = twoTabs()
        showTabTitles(tabs, show = false)

        showTabTitles(tabs, show = true)

        assertFalse(isIcon(overview) || isIcon(assignments))
        assertNull(overview.getUserData(ToolWindow.SHOW_CONTENT_TAB_LABEL_TEXT))
        assertEquals(
            listOf(null, null),
            tabs.map { it.first.description },
            "a title already on screen would only be repeated"
        )
        assertEquals(listOf(OVERVIEW, ASSIGNMENTS), tabs.map { it.first.displayName })
    }

    @Test
    fun `every flip changes a description, which is what redraws the strip`() {
        val (tabs, overview, _) = twoTabs()

        val descriptions = listOf(true, false, true, false).map { show ->
            showTabTitles(tabs, show)
            overview.description
        }

        assertEquals(
            listOf(null, OVERVIEW, null, OVERVIEW),
            descriptions,
            "an unchanged description raises no event, and the tabs would keep their old look"
        )
    }


    @Test
    fun `titles collapse below the scaled breakpoint and expand after the gap`() {
        val applied = mutableListOf<Boolean>()
        val switch = TabTitleSwitch { applied += it }

        switch.resized(JBUI.scale(380))
        switch.resized(JBUI.scale(360))
        assertTrue(applied.isEmpty())
        switch.resized(JBUI.scale(359))
        assertEquals(listOf(false), applied)
        switch.resized(JBUI.scale(360))
        switch.resized(JBUI.scale(379))
        assertEquals(listOf(false), applied)
        switch.resized(JBUI.scale(380))
        switch.resized(JBUI.scale(450))
        assertEquals(listOf(false, true), applied)
    }

    @Test
    fun `zero width before layout does not hide titles`() {
        val applied = mutableListOf<Boolean>()
        val switch = TabTitleSwitch { applied += it }
        switch.resized(0)
        assertTrue(applied.isEmpty())
    }

    private companion object {
        @NonNls
        const val OVERVIEW = "Course"

        @NonNls
        const val ASSIGNMENTS = "Assignments"
    }
}
