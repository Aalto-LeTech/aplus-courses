package fi.aalto.cs.apluscourses.ui.news

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.IntelliJSpacingConfiguration
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.model.news.NewsTree
import icons.PluginIcons
import java.awt.Font
import javax.swing.ScrollPaneConstants

class NewsView(private val toolWindow: ToolWindow, private val project: Project) : SimpleToolWindowPanel(true, true) {
    private var news: NewsTree? = null
    private var shortTab = false
    private val shortTabTitle = "📰"
    private val longTabTitle = "News"
    private val tabTitle
        get() = if (shortTab) shortTabTitle else longTabTitle

    private val notificationDot = """<span style="color: #FF0090;">●</span>"""
    private val unreadTabTitle
        get() = """<html><body>${
            if (shortTab)
                notificationDot
            else
                """<span>${longTabTitle} </span>${notificationDot}"""
        }</body></html>"""

    init {
        val content = JBScrollPane()
        setContent(content)
    }

    /**
     * Sets the view model of this view, or does nothing if the given view model is null.
     */
    fun viewModelChanged(newsTree: NewsTree?) {
        ApplicationManager.getApplication().invokeLater {
            println("newsview update")
            this.news = newsTree
            if (newsTree == null) {
                return@invokeLater
            }
            println("newsview update 2")
            val content = JBScrollPane((panel {
                panel {
                    customizeSpacingConfiguration(object : IntelliJSpacingConfiguration() {
                        override val verticalComponentGap: Int = 1
                    }) {
                        for (node in newsTree.news) {
                            row {
                                text(node.title).applyToComponent {
                                    font = JBFont.regular().biggerOn(6f).deriveFont(Font.BOLD)
                                }.resizableColumn()
                                if (!node.isRead) {
                                    icon(PluginIcons.A_PLUS_NEW)
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
        toolWindow.contentManager.getContent(this).displayName =
            if (currentNews != null && currentNews.unreadCount() > 0) unreadTabTitle else tabTitle
    }
}