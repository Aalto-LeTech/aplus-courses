package fi.aalto.cs.apluscourses.ui.news

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.dsl.gridLayout.UnscaledGapsY
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.presentation.news.NewsTreeViewModel
import fi.aalto.cs.apluscourses.ui.base.TreeView
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
import icons.PluginIcons
import java.awt.Font
import javax.swing.ScrollPaneConstants

class NewsView(private val toolWindow: ToolWindow) : SimpleToolWindowPanel(true, true) {
    val newsTree: TreeView = TreeView()

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
    fun viewModelChanged(viewModel: NewsTreeViewModel?) {
        ApplicationManager.getApplication().invokeLater(
            {
                newsTree.setViewModel(viewModel)
                if (viewModel == null) {
                    return@invokeLater
                }
                val content = JBScrollPane((panel {
                    panel {
                        for (node in viewModel.model.news) {
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
                if (viewModel.model.unreadCount() > 0L) {
                    toolWindow.contentManager.getContent(this).displayName =
                        """<html><body>News <span style="color: #FF0090;">●</span></body></html>"""
                } else {
                    toolWindow.contentManager.getContent(this).displayName = "News"
                }
//                title!!.text = viewModel.title
            }, ModalityState.any()
        )
    }

//    private fun createUIComponents() {
//        pane = ScrollPaneFactory.createScrollPane(basePanel)
//        title = JLabel()
//        newsTree = TreeView()
//        newsTree!!.cellRenderer = NewsTreeRenderer()
//        newsTree!!.rowHeight = -1
//    }
}
