package fi.aalto.cs.apluscourses.ui.news

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.IntelliJSpacingConfiguration
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.application
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.news.NewsList
import java.awt.Font

class NewsPanel {
    var content: DialogPanel = panel {
        row {
            label("News")
        }
    }
    private var news: NewsList? = null
//    private var shortTab = false
//    private val shortTabTitle = message("toolwindows.APlusToolWindowFactory.tabs.newsShort")
//    private val longTabTitle = message("toolwindows.APlusToolWindowFactory.tabs.news")
//    private val tabTitle
//        get() = if (shortTab) shortTabTitle else longTabTitle

//    private val notificationDot = message("ui.NewsView.notificationDot")
//    private val unreadTabTitle
//        get() = """<html><body>${
//            if (shortTab)
//                notificationDot
//            else
//                message("ui.NewsView.titleWithDot")
//        }</body></html>"""

//    init {
//        val content = JBScrollPane()
//        setContent(content)
//        emptyText.text = message("ui.NewsView.noNews")
//    }

    /**
     * Sets the view model of this view, or does nothing if the given view model is null.
     */
    fun viewModelChanged(newsList: NewsList?) {
        application.invokeLater {
            this.news = newsList
            if (newsList == null || newsList.news.isEmpty()) {
                content.removeAll()
                return@invokeLater
            }
            val newsPanel = panel {
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
            }
            content = newsPanel
//            updateTitle()
//            updateUI()
        }
    }

//    fun setShortTab(short: Boolean) {
//        shortTab = short
//        updateTitle()
//    }
//
//    private fun updateTitle() {
//        val currentNews = news
//        val content = toolWindow.contentManager.getContent(this) ?: return
//        content.displayName =
//            if (currentNews != null && currentNews.unreadCount() > 0) unreadTabTitle else tabTitle
//    }
}
