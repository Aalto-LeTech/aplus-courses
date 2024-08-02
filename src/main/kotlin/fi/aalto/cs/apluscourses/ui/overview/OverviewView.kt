package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.ide.troubleshooting.scale
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.LightColors
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.scale.ScaleContext
import com.intellij.ui.util.minimumHeight
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.ui.BannerPanel
import icons.PluginIcons
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.periodUntil
import java.awt.*
import java.awt.event.ComponentAdapter
import java.net.URI
import java.net.URL
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

class OverviewView(private val project: Project) : SimpleToolWindowPanel(true, true) {
    companion object {
        private val BANNER =
            IconLoader.toImage(
                IconLoader.getIcon("/META-INF/images/O1-2024.png", OverviewView::class.java.classLoader),
                ScaleContext.createIdentity()
            )
    }

    private var banner: ResponsiveImagePanel? = null

    private var panel = createPanel()

    fun update() {
        panel = createPanel()
        val content = JBScrollPane(panel)
        content.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        setContent(content)
    }


    private fun loadingPanel(): DialogPanel {
//        val icon = IconUtil.scale(PluginIcons.A_PLUS_LOADING, this, 1.5f)
        return panel {
            row {
                icon(PluginIcons.A_PLUS_LOADING).resizableColumn().align(Align.CENTER)
            }.resizableRow()
        }
    }

    private fun createPanel(): DialogPanel {
        val course = CourseManager.course(project) ?: return loadingPanel()
        val user = CourseManager.user(project) ?: return loadingPanel()
        val points = ExercisesUpdaterService.getInstance(project).state.pointsByDifficulty
        val pointsText = // Format: "<b>A</b> 10<br><b>B</b> 5<br><b>C</b> 0"
            points
                ?.entries
                ?.filter { !course.optionalCategories.contains(it.key) }
                ?.sortedBy { it.key }
                ?.joinToString("<br>") { (key, value) -> "<b>$key</b> $value" }
        val banner = ResponsiveImagePanel(course.imageUrl, this.width)
        this.banner = banner
        return panel {
            row { cell(banner) }
            row { cell(BannerPanel(MyBundle.message("ui.BannerView.courseEnded"), BannerPanel.BannerType.ERROR)) }
            panel {
                row {
                    text(course.name).applyToComponent {
                        font = JBFont.regular().biggerOn(7f).deriveFont(Font.PLAIN)
                    }
                }
                row {
                    text(user.userName)
                }

                if (pointsText == null) {
                    row {
                        icon(PluginIcons.A_PLUS_LOADING).resizableColumn().align(Align.CENTER)
                    }.resizableRow()
                } else if (pointsText.isNotEmpty()) {
                    separator()
                    row {
                        text("Points collected")
                    }
                    row {
                        text(pointsText)
                    }
                    separator()
                    row {
                        comment("Week 2 closing 21.2.2024 21:00 (in 4 hours)")
                    }
                }

            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private fun timeInfo(closingTime: String): String {
//        val closing = Instant.parse(closingTime)
//        val now = Clock.System.now()
//        val diff = now.periodUntil(closing)
//        val hours = diff.toHours()
//        val minutes = diff.toMinutes() % 60
//        return "Closing ${hours}h ${minutes}m"
        return ""
    }

    init {
        toolbar = null
        val content = JBScrollPane(panel)
        setContent(content)
    }

    private var oldWidth = 0
    override fun getWidth(): Int {
        val newWidth = super.getWidth()

        if (newWidth != oldWidth && this.isShowing) {
            oldWidth = newWidth
            banner?.updateWidth(newWidth)
        }
        return newWidth
    }
}

private class ResponsiveImagePanel(url: String, width: Int) : JPanel() {
    private var image: Image? = null
    private var width: Int = 100
    private val heightMultiplier: Double

    init {
        val icon = IconLoader.findIcon(URI(url).toURL(), true)
        println("icon $icon ${icon?.iconWidth} ${url}")
        image = icon?.let {
            IconLoader.toImage(
                it,
                ScaleContext.createIdentity()
            )
        }
        if (icon != null) {
            heightMultiplier = icon.iconHeight.toDouble() / icon.iconWidth.toDouble()
        } else {
            heightMultiplier = 1.0
        }

        updateWidth(width)
    }

    fun updateWidth(width: Int) {
        this.width = width
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        image?.let {
            val graphics2D = g.create() as Graphics2D
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics2D.drawImage(it, 0, 0, width, height, this)
        }
    }

    override fun getWidth() = width
    override fun getHeight() = (width * heightMultiplier).toInt()

    override fun getPreferredSize() = Dimension(width, height)
}