package fi.aalto.cs.apluscourses.ui.news

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.dsl.gridLayout.UnscaledGapsY
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.model.news.NewsTree
import fi.aalto.cs.apluscourses.ui.base.TreeView
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
import icons.PluginIcons
import java.awt.Font
import javax.swing.ScrollPaneConstants

class NewsView(private val toolWindow: ToolWindow, private val project: Project) : SimpleToolWindowPanel(true, true) {
    private val newsTree: TreeView = TreeView()
    private var news: NewsTree? = null
    private var shortTab = false
    private val shortTabTitle = "📰"
    private val longTabTitle = "News"
    private fun tabTitle() = if (shortTab) shortTabTitle else longTabTitle
    private fun unreadTabTitle() =
        """<html><body><span>${tabTitle()} </span><span style="color: #FF0090;">●</span></body></html>"""

    /**
     * Creates an ExerciseView that uses mainViewModel to dynamically adjust its UI components.
     */
    init {
        val content = JBScrollPane(newsTree)
        setContent(content)

        newsTree.cellRenderer = NewsTreeRenderer()
        newsTree.rowHeight = -1
//        basePanel!!.putClientProperty(NewsView::class.java.name, this)
//        pane!!.border = BorderFactory.createMatteBorder(1, 0, 1, 0, JBColor.border())
//        newsTree!!.emptyText.setText("")
//        newsTree!!.isOpaque = true
        val rowHeight = newsTree.rowHeight
        // Row height returns <= 0 on some platforms, so a default alternative is needed
        content.verticalScrollBar.unitIncrement = if (rowHeight <= 0) 20 else rowHeight
        newsTree.emptyText.setText(PluginResourceBundle.getText("ui.toolWindow.subTab.news.noNews"))
    }

    //    override fun onExpandSplitter() {
//        // Row height is wrong after loading while not expanded, unless this is called.
//        ApplicationManager.getApplication().invokeLater { newsTree!!.addNotify() }
//    }

    /**
     * Sets the view model of this view, or does nothing if the given view model is null.
     */
    fun viewModelChanged(newsTree: NewsTree?) {
        ApplicationManager.getApplication().invokeLater {
            println("newsview update")
//                newsTree.setViewModel(viewModel)
            this.news = newsTree
            if (newsTree == null) {
                return@invokeLater
            }
            println("newsview update 2")
            val content = JBScrollPane((panel {
                panel {
                    for (node in newsTree.news) {
                        row {
                            text(node.title).applyToComponent {
                                font = JBFont.regular().biggerOn(6f).deriveFont(Font.BOLD)
                            }.resizableColumn()
                            if (!node.isRead) {
                                icon(PluginIcons.A_PLUS_NEW)
                            }
                        }.customize(UnscaledGapsY(0, 0))
                        row {
                            text(node.publishTimeInfo).applyToComponent {
                                foreground = JBColor.GRAY
                            }
                        }.customize(UnscaledGapsY(0, 0))
                        row {
                            text(node.body)
                        }
                        separator()
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
            if (currentNews != null && currentNews.unreadCount() > 0) {
                unreadTabTitle()
            } else {
                tabTitle()
            }
    }
}
