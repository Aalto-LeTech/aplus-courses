package fi.aalto.cs.apluscourses.ui.news

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.IntelliJSpacingConfiguration
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.ui.SettleScheduler
import java.awt.*
import javax.swing.JButton
import javax.swing.JPanel

/** Displays course news in the overview. Clicking an unread chip invokes [onMarkRead]. */
class NewsPanel(
    private val settle: SettleScheduler,
    private val now: () -> Long = System::currentTimeMillis,
    private val onSizeChanged: () -> Unit = {},
    private val onMarkRead: () -> Unit
) : JPanel(null) {
    private var rendered: List<NewsItem> = emptyList()
    private var content: DialogPanel? = null

    private var wrappedWidth = 0
    private var contentHeight = 0
    private var wrappedAt = 0L

    init {
        isOpaque = false
    }

    @RequiresEdt
    fun update(items: List<NewsItem>) {
        if (items == rendered) return
        rendered = items
        removeAll()
        content = if (items.isEmpty()) null else newsContent(items).also { add(it) }
        wrap(width)
        revalidate()
        repaint()
    }

    /**
     * Throttles resize wrapping to once per [THROTTLE_MILLIS].
     * Schedules a final wrap after resizing stops.
     */
    override fun doLayout() {
        val child = content ?: return
        if (width > 0 && width != wrappedWidth) {
            if (now() - wrappedAt >= THROTTLE_MILLIS) wrap(width)
            else settle.after(SETTLE_MILLIS) { rewrapIfResized() }
        }
        child.setBounds(0, 0, wrappedWidth, contentHeight)
    }

    override fun getPreferredSize(): Dimension = Dimension(wrappedWidth, contentHeight)

    /** A zero minimum width lets the overview determine text wrapping. */
    override fun getMinimumSize(): Dimension = Dimension(0, contentHeight)

    override fun updateUI() {
        super.updateUI()
        // Look-and-feel changes finish updating children after this method returns.
        if (content != null) application.invokeLater { wrap(width) }
    }

    private fun rewrapIfResized() {
        if (width > 0 && width != wrappedWidth) wrap(width)
    }

    /**
     * The DSL grid needs layout before its labels report wrapped heights.
     * Measures with extra height before applying the resulting height.
     */
    private fun wrap(toWidth: Int) {
        val child = content
        if (child == null || toWidth <= 0) {
            wrappedWidth = 0
            contentHeight = 0
            return
        }
        child.setBounds(0, 0, toWidth, MEASURING_HEIGHT)
        layOutTree(child)
        val height = child.preferredSize.height
        child.setBounds(0, 0, toWidth, height)
        layOutTree(child)
        val sizeChanged = wrappedWidth != toWidth || contentHeight != height
        wrappedWidth = toWidth
        contentHeight = height
        wrappedAt = now()
        revalidate()
        if (sizeChanged) {
            onSizeChanged()
        }
    }

    private fun newsContent(items: List<NewsItem>): DialogPanel = panel {
        customizeSpacingConfiguration(object : IntelliJSpacingConfiguration() {
            override val verticalComponentGap: Int = 1
        }) {
            for (item in items) {
                row {
                    text(item.title).applyToComponent {
                        font = JBFont.regular().biggerOn(2f).deriveFont(Font.BOLD)
                    }.resizableColumn()
                    if (!item.isRead) {
                        cell(JButton(CoursesIcons.NewChip).apply {
                            toolTipText = message("ui.NewsPanel.markAsRead")
                            accessibleContext.accessibleName = toolTipText
                            isBorderPainted = false
                            isContentAreaFilled = false
                            isOpaque = false
                            margin = JBUI.emptyInsets()
                            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            addActionListener { onMarkRead() }
                        }).align(AlignY.TOP)
                    }
                }
                row {
                    text(item.publishTimeInfo).applyToComponent {
                        foreground = JBColor.GRAY
                    }
                }.bottomGap(BottomGap.SMALL)
                row { text(item.body) }.bottomGap(BottomGap.SMALL)
                separator().bottomGap(BottomGap.SMALL)
            }
        }
    }.apply { isOpaque = false }
}

private fun layOutTree(component: Component) {
    if (component !is Container) return
    component.doLayout()
    component.components.forEach(::layOutTree)
}

/** Minimum interval between wraps during resizing, in milliseconds. */
private const val THROTTLE_MILLIS = 100L

/** Delay before wrapping after the last resize, in milliseconds. */
private const val SETTLE_MILLIS = 120L

/** Height in pixels available during the initial wrapping measurement. */
private const val MEASURING_HEIGHT = 32000
