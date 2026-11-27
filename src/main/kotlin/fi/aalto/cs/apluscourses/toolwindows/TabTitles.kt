package fi.aalto.cs.apluscourses.toolwindows

import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons

/** Keeps tab names for overflow menus and single-tab headers while hiding visible labels. */
internal fun showTabTitles(tabs: List<Pair<Content, String>>, show: Boolean) {
    tabs.forEach { (tab, title) ->
        tab.putUserData(ToolWindow.SHOW_CONTENT_TAB_LABEL_TEXT, if (show) null else false)
        val unread = tab.getUserData(COURSE_NEWS_UNREAD)
        tab.putUserData(ToolWindow.SHOW_CONTENT_ICON, !show || unread == true)
        if (unread != null) tab.icon = courseTabIcon(unread, show)
        // These keys emit no change event. Updating the description repaints the tab strip.
        tab.description = tabDescription(tab, title, show)
    }
}

private val COURSE_NEWS_UNREAD = Key.create<Boolean>("aplus.courseNewsUnread")

internal fun updateCourseTabNews(tab: Content, unread: Boolean) {
    tab.putUserData(COURSE_NEWS_UNREAD, unread)
    val showTitle = tab.getUserData(ToolWindow.SHOW_CONTENT_TAB_LABEL_TEXT) != false
    tab.icon = courseTabIcon(unread, showTitle)
    tab.putUserData(ToolWindow.SHOW_CONTENT_ICON, !showTitle || unread)
    tab.description = tabDescription(tab, tab.displayName, showTitle)
}

private fun courseTabIcon(unread: Boolean, showTitle: Boolean) = when {
    !unread -> CoursesIcons.LogoColor
    showTitle -> CoursesIcons.UnreadNews
    else -> CoursesIcons.LogoUnread
}

private fun tabDescription(tab: Content, title: String, showTitle: Boolean): String? =
    if (tab.getUserData(COURSE_NEWS_UNREAD) == true) {
        val unread = message("toolwindows.APlusToolWindowFactory.tabs.overviewUnread")
        if (showTitle) unread else "$title: $unread"
    } else if (showTitle) null else title

internal class TabTitleSwitch(private val show: (Boolean) -> Unit) {
    private var titlesShown = true

    fun resized(width: Int) {
        if (width <= 0) return
        val next = if (titlesShown) width >= JBUI.scale(COLLAPSE_WIDTH)
        else width >= JBUI.scale(EXPAND_WIDTH)
        if (next == titlesShown) return
        titlesShown = next
        show(next)
    }
}

// Logical pixels. Separate thresholds prevent flickering near the breakpoint.
private const val COLLAPSE_WIDTH = 360
private const val EXPAND_WIDTH = 380
