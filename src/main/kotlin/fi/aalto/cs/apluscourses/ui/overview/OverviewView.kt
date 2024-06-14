package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.icons.CachedImageIcon
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBFont
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.ImageIcon

class OverviewView(private val project: Project) : SimpleToolWindowPanel(true, true) {
    companion object {
        private val BANNER =
            IconLoader.toImage(
                IconLoader.getIcon("/META-INF/images/O1-2024.png", OverviewView::class.java.classLoader),
                ScaleContext.createIdentity()
            )
    }

    private val stretchIcon = StretchIcon(BANNER!!)
    private val panel = panel {
        row {
            icon(stretchIcon)
                .align(AlignX.FILL + AlignY.TOP)
                .resizableColumn().apply {
                    addComponentListener(object : ComponentListener {
                        override fun componentResized(e: ComponentEvent) {
                            stretchIcon.paintIcon(
                                e.component,
                                e.component.graphics,
                                this@apply.component.x,
                                this@apply.component.y
                            )
                            revalidate()
                        }

                        override fun componentMoved(e: ComponentEvent?) {}
                        override fun componentShown(e: ComponentEvent) {
                            revalidate()
                            stretchIcon.paintIcon(
                                e.component,
                                e.component.graphics,
                                this@apply.component.x,
                                this@apply.component.y
                            )
                            revalidate()
                        }

                        override fun componentHidden(e: ComponentEvent?) {}
                    })
                }
//                .applyToComponent {
//                    minimumSize = JBDimension(1, BANNER.iconHeight / 2)
//                    preferredSize = JBDimension(1, BANNER.iconHeight / 2)
//                    isOpaque = true
//                }
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
        val content = JBScrollPane(panel)
        setContent(panel)
    }

    override fun setSize(d: Dimension) {
        println(d)
        super.setSize(d)
    }
}