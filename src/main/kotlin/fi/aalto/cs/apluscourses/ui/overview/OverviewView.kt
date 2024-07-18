package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.htmlComponent
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.icons.CachedImageIcon
import com.intellij.ui.scale.ScaleContext
import com.intellij.ui.util.preferredWidth
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBFont
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Image
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.ImageIcon
import javax.swing.JPanel

class OverviewView(private val project: Project) : SimpleToolWindowPanel(true, true) {
    companion object {
        private val BANNER =
            IconLoader.toImage(
                IconLoader.getIcon("/META-INF/images/O1-2024.png", OverviewView::class.java.classLoader),
                ScaleContext.createIdentity()
            )
    }

    private val panel = panel {
        row {
            cell(ResponsiveImagePanel())
                .resizableColumn()
        }.topGap(TopGap.NONE)
        panel {
            row {
                text("O1 2024").applyToComponent {
                    font = JBFont.regular().biggerOn(7f).deriveFont(Font.PLAIN)
                }
            }
            row {
                text("Jaakko Nakaza")
            }
            separator()
            row {
                text("Points collected")
            }
            row {
                text("<b>A</b> 10<br><b>B</b> 5<br><b>C</b> 0")
            }
            separator()
            row {
                comment("Week 2 closing 21.2.2024 21:00 (in 4 hours)")
            }
        }.customize(UnscaledGaps(32, 32, 16, 32))
    }

    init {
        toolbar = null
        val content = JBScrollPane(panel)
        setContent(panel)
    }
}

private class ResponsiveImagePanel : JPanel() {
    private var image: Image? = null
    private var width = 100
    private val heightMultiplier: Double

    init {
        val icon = IconLoader.getIcon("/META-INF/images/O1-2024.png", OverviewView::class.java.classLoader)
        image = IconLoader.toImage(
            icon,
            ScaleContext.createIdentity()
        )
        heightMultiplier = icon.iconHeight.toDouble() / icon.iconWidth.toDouble()

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                repaint()
            }

            override fun componentShown(e: ComponentEvent?) {
                repaint()
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        image?.let {
            width = this.parent.width
            g.drawImage(it, 0, 0, width, height, this)
        }
    }

    override fun getWidth(): Int = width
    override fun getHeight(): Int = (width * heightMultiplier).toInt()
    override fun getPreferredSize(): Dimension = Dimension(width, height)
}