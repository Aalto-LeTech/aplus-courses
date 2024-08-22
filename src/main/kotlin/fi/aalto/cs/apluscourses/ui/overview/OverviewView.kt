package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.icons.AllIcons
import com.intellij.ide.actions.NewProjectAction
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.api.CourseConfig.Grading
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.InitializationStatus
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.ui.BannerPanel
import fi.aalto.cs.apluscourses.ui.TokenForm
import fi.aalto.cs.apluscourses.ui.Utils.loadingPanel
import fi.aalto.cs.apluscourses.ui.Utils.myActionLink
import fi.aalto.cs.apluscourses.utils.DateDifferenceFormatter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.awt.*
import java.net.URI
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.ScrollPaneConstants
import kotlin.math.max

class OverviewView(private val project: Project) : SimpleToolWindowPanel(true, true) {
    private var banner: ResponsiveImagePanel? = null

    private var panel = createPanel()

    fun update(loading: Boolean = false) {
        panel = createPanel(loading)
        val content = JBScrollPane(panel)
        content.horizontalScrollBarPolicy =
            if (isMainPanel) ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED else ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        setContent(content)
    }

    private fun authPanel(): DialogPanel {
        return panel {
            val courseName = CourseManager.getInstance(project).state.courseName ?: ""
            val tokenForm = TokenForm(project) {
                update(loading = true)
                CourseManager.getInstance(project).restart()
            }
            panel {
                row {
                    text("Welcome to $courseName").applyToComponent {
                        font = JBFont.h1()
                    }.comment("You need to log in to access the course content:")
                }
                with(tokenForm) {
                    token()
                    validation()
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private fun networkErrorPanel(): DialogPanel {
        return panel {
            panel {
                row {
                    text("The plugin encountered a network error").applyToComponent {
                        font = JBFont.h1()
                    }.comment("Please check your internet connection and that A+ is accessible.")
                }
                row {
                    button("Refresh") {
                        update(loading = true)
                        CourseManager.getInstance(project).restart()
                    }
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private fun notEnrolledPanel(): DialogPanel {
        return panel {
            val courseName = CourseManager.getInstance(project).state.courseName
            panel {
                row {
                    text("Looks like you are not enrolled on this course").applyToComponent {
                        font = JBFont.h1()
                    }.comment("Please check the ${courseName ?: "course"} page on A+.")
                }
                row {
                    button("Refresh") {
                        update(loading = true)
                        CourseManager.getInstance(project).restart()
                    }
                }

            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private fun notACoursePanel(): DialogPanel {
        return panel {
            panel {
                row {
                    text("This project is not linked to a course").applyToComponent {
                        font = JBFont.h1()
                    }.comment("Create a new A+ Courses project to access a course.")
                }
                row {
                    myActionLink("Create new project", CoursesIcons.LogoColor, NewProjectAction())
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private fun initializationIoErrorPanel(): DialogPanel {
        return panel {
            panel {
                row {
                    text("The plugin encountered a network error during initialization").applyToComponent {
                        font = JBFont.h1()
                    }.comment("Please check your internet connection and that A+ is accessible.")
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private var isMainPanel = false
    private val mainPanelMaxWidth = 300

    private fun createPanel(loading: Boolean = false): DialogPanel {
        isMainPanel = false
        if (InitializationStatus.isNotCourse(project)) {
            return notACoursePanel()
        }
        if (InitializationStatus.isIoError(project)) {
            return initializationIoErrorPanel()
        }
        if (loading) return loadingPanel()
        val authenticated = CourseManager.authenticated(project) ?: return loadingPanel()
        if (!authenticated) return authPanel()
        val error = CourseManager.error(project)
        if (error == CourseManager.Error.NETWORK_ERROR) {
            return networkErrorPanel()
        } else if (error == CourseManager.Error.NOT_ENROLLED) {
            return notEnrolledPanel()
        }
        val course = CourseManager.course(project) ?: return loadingPanel()
        val user = CourseManager.user(project) ?: return loadingPanel()
        val isCourseEnded = CourseManager.isCourseEnded(project)
        val weeks = ExercisesUpdater.getInstance(project).state.exerciseGroups
        val points = ExercisesUpdater.getInstance(project).state.userPointsForCategories
            ?.filter { !course.optionalCategories.contains(it.key) }
            ?.toList()
            ?.sortedBy { it.first } // Show categories in alphabetical order
        val grading = CourseManager.getInstance(project).state.grading
        val gradeData = grading?.let { grade(it, points?.toMap() ?: emptyMap()) }
        val grade = gradeData?.grade
        val pointsUntilNextGrade = gradeData?.pointsUntilNext
        val maxPointsOfNextGrade = gradeData?.maxOfNext
        val maxPoints = ExercisesUpdater.getInstance(project).state.maxPointsForCategories
        val banner = ResponsiveImagePanel(course.imageUrl, width = this.width, maxWidth = mainPanelMaxWidth)
        this.banner = banner
        isMainPanel = true
        val mainPanel = panel {
            row { cell(banner) }
            if (isCourseEnded) {
                row { cell(BannerPanel(MyBundle.message("ui.BannerView.courseEnded"), BannerPanel.BannerType.ERROR)) }
            }
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
                            icon(CoursesIcons.Loading).resizableColumn().align(Align.CENTER)
                        }.resizableRow()
                    } else {
                        group(indent = false) {
                            row {
                                text("Points collected").comment(grade?.let { "Grade $it" })
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
                            if (pointsUntilNextGrade != null && maxPointsOfNextGrade != null) {
                                row {
                                    text("Points until next grade")
                                }.topGap(TopGap.SMALL)
                                pointsUntilNextGrade.map { (category, pointsUntilNext) ->
                                    val maxPointsForCategory =
                                        if (pointsUntilNext == 0) 1 // Full progress bar for completed categories
                                        else maxPointsOfNextGrade[category] ?: 0
                                    val progressPoints = maxPointsForCategory - pointsUntilNext

                                    row {
                                        text(category.replaceFirstChar { it.uppercase() }).bold()
                                        cell(JProgressBar(0, maxPointsForCategory))
                                            .applyToComponent { value = progressPoints }
                                            .resizableColumn().align(AlignX.FILL)
                                        text(pointsUntilNext.toString()).applyToComponent {
                                            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                                        }
                                    }.layout(RowLayout.PARENT_GRID)
                                }
                            }
                        }
                        separator().bottomGap(BottomGap.MEDIUM)
                        weekClosingTime(weeks)
                        row {
                            link("Plugin settings") {
                                ShowSettingsUtil.getInstance().showSettingsDialog(project, "A+ Courses")
                            }.applyToComponent {
                                icon = AllIcons.General.Settings
                                isFocusPainted = false
                            }
                        }
                        row {
                            browserLink("Course page", course.htmlUrl).applyToComponent {
                                setIcon(CoursesIcons.LogoColor, atRight = false)
                                isFocusPainted = false
                            }
                        }
                    }
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
        mainPanel.preferredSize = Dimension(mainPanelMaxWidth, Int.MAX_VALUE)
        return mainPanel
    }

    private class Grade(val grade: String?, val pointsUntilNext: Map<String, Int>?, val maxOfNext: Map<String, Int>?)

    private fun grade(grading: Grading, points: Map<String, Int>): Grade? {
        val style = grading.style
        val gradePoints = grading.points

        return when (style) {
            "o1" -> {
                val a = points["A"] ?: return null
                val b = points["B"] ?: return null
                val c = points["C"] ?: return null

                val currentGrade = gradePoints.entries.findLast { (_, requiredPoints) ->
                    a >= requiredPoints["A"]!! &&
                            b >= requiredPoints["B"]!! &&
                            c >= requiredPoints["C"]!!
                }

                val nextGrade = gradePoints.entries.firstOrNull { (_, requiredPoints) ->
                    a < requiredPoints["A"]!! ||
                            b < requiredPoints["B"]!! ||
                            c < requiredPoints["C"]!!
                }

                val pointsToNextGrade = if (nextGrade != null) {
                    mapOf(
                        "A" to maxOf(0, nextGrade.value["A"]!! - a),
                        "B" to maxOf(0, nextGrade.value["B"]!! - b),
                        "C" to maxOf(0, nextGrade.value["C"]!! - c)
                    )
                } else {
                    mapOf("A" to 0, "B" to 0, "C" to 0)
                }

                val maxOfNext = mapOf(
                    "A" to (nextGrade?.value["A"] ?: 0),
                    "B" to (nextGrade?.value["B"] ?: 0),
                    "C" to (nextGrade?.value["C"] ?: 0)
                )

                Grade(currentGrade?.key, pointsToNextGrade, maxOfNext)
            }

            "total" -> {
                val totalPoints = points.values.sum()

                val currentGrade = gradePoints.entries.findLast { (_, requiredPoints) ->
                    totalPoints >= requiredPoints["total"]!!
                }

                val nextGrade = gradePoints.entries.firstOrNull { (_, requiredPoints) ->
                    totalPoints < requiredPoints["total"]!!
                }

                val pointsToNextGrade = if (nextGrade != null) {
                    mapOf(
                        "total" to maxOf(0, nextGrade.value["total"]!! - totalPoints)
                    )
                } else {
                    mapOf("total" to 0)
                }

                val maxOfNext = mapOf("total" to (nextGrade?.value["total"] ?: 0))

                Grade(currentGrade?.key, pointsToNextGrade, maxOfNext)
            }

            else -> {
                null
            }
        }
    }

    fun Panel.weekClosingTime(weeks: List<ExerciseGroup>) {
        val now = Clock.System.now()
        val currentWeek = weeks.find { it.closingTime != null && now < Instant.parse(it.closingTime) }
        val weekNumber = weeks.indexOf(currentWeek) + 1
        if (currentWeek != null) {
            val closingTime = Instant.parse(currentWeek.closingTime!!)
            val localDateTime = closingTime.toLocalDateTime(TimeZone.currentSystemDefault())
            val formattedDateTime = localDateTime.run {
                "${dayOfMonth}.${monthNumber}.${year} ${hour.toString().padStart(2, '0')}:${
                    minute.toString().padStart(2, '0')
                }"
            }
            val howLongLeft = DateDifferenceFormatter.formatTimeSinceNow(closingTime)
            row {
                text("Week $weekNumber closing $formattedDateTime ($howLongLeft)").applyToComponent {
                    foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                }
            }.bottomGap(BottomGap.SMALL)
        }
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

class ResponsiveImagePanel(url: String? = null, icon: Icon? = null, width: Int, val maxWidth: Int = width) : JPanel() {
    private var image: Image? = null
    private var width: Int = 100
    private val heightMultiplier: Double

    init {
        val icon = icon ?: IconLoader.findIcon(URI(url!!).toURL(), true)
        println("icon $icon ${icon?.iconWidth} $url")
        image = icon?.let {
            IconLoader.toImage(
                it,
                ScaleContext.createIdentity()
            )
        }
        heightMultiplier = if (icon != null)
            icon.iconHeight.toDouble() / icon.iconWidth.toDouble()
        else 1.0

        updateWidth(width)
    }

    fun updateWidth(width: Int) {
        this.width = max(width, maxWidth)
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