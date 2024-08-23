package fi.aalto.cs.apluscourses.ui.news

import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.IntelliJSpacingConfiguration
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.news.NewsList
import java.awt.Font
import javax.swing.ScrollPaneConstants

class NewsView(private val toolWindow: ToolWindow) : SimpleToolWindowPanel(true, true) {
    private var news: NewsList? = null
    private var shortTab = false
    private val shortTabTitle = message("toolwindows.APlusToolWindowFactory.tabs.newsShort")
    private val longTabTitle = message("toolwindows.APlusToolWindowFactory.tabs.news")
    private val tabTitle
        get() = if (shortTab) shortTabTitle else longTabTitle

    private val notificationDot = message("ui.NewsView.notificationDot")
    private val unreadTabTitle
        get() = """<html><body>${
            if (shortTab)
                notificationDot
            else
                message("ui.NewsView.titleWithDot")
        }</body></html>"""

    init {
        val content = JBScrollPane()
        setContent(content)
        emptyText.text = message("ui.NewsView.noNews")
    }

    /**
     * Sets the view model of this view, or does nothing if the given view model is null.
     */
    fun viewModelChanged(newsList: NewsList?) {
        application.invokeLater {
            this.news = newsList
            if (newsList == null || newsList.news.isEmpty()) {
                remove(content)
                return@invokeLater
            }
            val content = JBScrollPane((panel {
                panel {
                    customizeSpacingConfiguration(object : IntelliJSpacingConfiguration() {
                        override val verticalComponentGap: Int = 1
                    }) {
                        for (node in newsList.news) {
                            row {
                                text(node.title).applyToComponent {
                                    font = JBFont.regular().biggerOn(6f).deriveFont(Font.BOLD)
                                }.resizableColumn()
                                if (!node.isRead) {
                                    icon(CoursesIcons.NewChip).align(AlignY.TOP)
                                }
                            }
                            row {
                                text(node.publishTimeInfo).applyToComponent {
                                    foreground = JBColor.GRAY
                                }
                            }.bottomGap(BottomGap.SMALL)
                            row { text(node.body) }.bottomGap(BottomGap.SMALL)
                            separator().bottomGap(BottomGap.SMALL)
                        }
                    }
                }.customize(UnscaledGaps(32, 32, 16, 32))
            }), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
            setContent(content)
            updateTitle()
            updateUI()
        }
    }

    fun setShortTab(short: Boolean) {
        shortTab = short
        updateTitle()
    }

    private fun updateTitle() {
        val currentNews = news
        val content = toolWindow.contentManager.getContent(this) ?: return
        content.displayName =
            if (currentNews != null && currentNews.unreadCount() > 0) unreadTabTitle else tabTitle
    }
}
