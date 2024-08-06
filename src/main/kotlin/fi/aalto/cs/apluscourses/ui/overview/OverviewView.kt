package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.credentialStore.OneTimeString
import com.intellij.icons.AllIcons
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.dal.TokenStorage
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.ui.BannerPanel
import icons.PluginIcons
import java.awt.*
import java.net.URI
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JProgressBar
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

    private var passwordField: JBPasswordField? = null// TODO duplicated
    private fun setToken() {
        TokenStorage.store(OneTimeString(passwordField!!.password))
        passwordField!!.text = ""
        CourseManager.getInstance(project).restart()
        ExercisesUpdaterService.getInstance(project).restart()
    }

    private fun createPanel(): DialogPanel {
        if (!TokenStorage.isTokenSet()) {
            passwordField = JBPasswordField()
            return panel {
                row("A+ token") {
                    cell(passwordField!!).columns(COLUMNS_MEDIUM)
                        .comment("<a href=\"https://plus.cs.aalto.fi/accounts/accounts/\">Show token in A+</a>")
                    button("Set") { setToken() }
                }
            }
        }
        val course = CourseManager.course(project) ?: return loadingPanel()
        val user = CourseManager.user(project) ?: return loadingPanel()
        val points = ExercisesUpdaterService.getInstance(project).state.userPointsForCategories
            ?.filter { !course.optionalCategories.contains(it.key) }
            ?.toList()
            ?.sortedBy { it.first } // Show categories in alphabetical order
        val maxPoints = ExercisesUpdaterService.getInstance(project).state.maxPointsForCategories
//        val pointsText = // Format: "<b>A</b> 10<br><b>B</b> 5<br><b>C</b> 0"
//            points
//                ?.entries
//                ?.filter { !course.optionalCategories.contains(it.key) }
//                ?.sortedBy { it.key }
//                ?.joinToString("<br>") { (key, value) -> "<b>$key</b> $value" }
        val banner = ResponsiveImagePanel(course.imageUrl, width = this.width)
        this.banner = banner
        return panel {
            row { cell(banner) }
            row { cell(BannerPanel(MyBundle.message("ui.BannerView.courseEnded"), BannerPanel.BannerType.ERROR)) }
            panel {
                customizeSpacingConfiguration(object : IntelliJSpacingConfiguration() {
                    override val verticalComponentGap: Int = 1
                }) {
                    row {
                        text(course.name).applyToComponent {
                            font = JBFont.h0()
                        }.comment(user.userName)
                    }.topGap(TopGap.SMALL)
                    if (points == null || maxPoints == null) {
                        row {
                            icon(PluginIcons.A_PLUS_LOADING).resizableColumn().align(Align.CENTER)
                        }.resizableRow()
                    } else {
                        group(indent = false) {
                            row {
                                text("Points collected").comment("Grade 4")
                            }.topGap(TopGap.MEDIUM)
                            points.map {
                                val (category, points) = it
                                val maxPoints = maxPoints[category]!!
                                row {
                                    text(category).bold()
                                    cell(JProgressBar(0, maxPoints))
                                        .applyToComponent { value = points }
                                        .resizableColumn().align(AlignX.FILL)
                                    text("${points}/${maxPoints}").applyToComponent {
                                        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                                    }
                                }.layout(RowLayout.PARENT_GRID)
                            }
                            row {
                                text("Points until next grade")
                            }.topGap(TopGap.SMALL)
                            points.map {
                                val (category, points) = it
                                val pointsTemp = if (category == "A") 355 else 1
                                val pointsLeft = if (category == "A") 345 else 0
                                val maxPoints = if (category == "A") 700 else 1
                                row {
                                    text(category).bold()
                                    cell(JProgressBar(0, maxPoints))
                                        .applyToComponent { value = pointsTemp }
                                        .resizableColumn().align(AlignX.FILL)
                                    text("$pointsLeft").applyToComponent {
                                        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                                    }
                                }.layout(RowLayout.PARENT_GRID)
                            }
                        }
                        separator().bottomGap(BottomGap.MEDIUM)
                        row {
                            text("Week 2 closing 21.2.2024 21:00 (in 4 hours)").applyToComponent {
                                foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                            }
                        }.bottomGap(BottomGap.SMALL)
                        row {
                            link("Plugin settings") {
                                ShowSettingsUtil.getInstance().showSettingsDialog(project, "A+ Courses")
                            }.applyToComponent {
                                icon = AllIcons.General.Settings
                            }
                        }
                        row {
                            browserLink("Course page", course.htmlUrl).applyToComponent {
                                setIcon(PluginIcons.A_PLUS_LOGO_COLOR, atRight = false)
                            }
                        }
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

class ResponsiveImagePanel(url: String? = null, icon: Icon? = null, width: Int) : JPanel() {
    private var image: Image? = null
    private var width: Int = 100
    private val heightMultiplier: Double

    init {
        val icon = icon ?: IconLoader.findIcon(URI(url!!).toURL(), true)
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