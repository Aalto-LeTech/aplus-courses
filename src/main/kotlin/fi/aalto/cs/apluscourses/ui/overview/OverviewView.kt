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
import fi.aalto.cs.apluscourses.MyBundle.message
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
import org.jetbrains.annotations.NonNls
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
        content.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
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
                    text(message("ui.OverviewView.auth.title", courseName)).applyToComponent {
                        font = JBFont.h1()
                    }.comment(message("ui.OverviewView.auth.description"))
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
                    text(message("ui.OverviewView.networkError.title")).applyToComponent {
                        font = JBFont.h1()
                    }.comment(message("ui.OverviewView.networkError.description"))
                }
                row {
                    button(message("ui.OverviewView.refresh")) {
                        update(loading = true)
                        CourseManager.getInstance(project).restart()
                    }
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private fun invalidTokenPanel(): DialogPanel {
        return panel {
            val tokenForm = TokenForm(project) {
                update(loading = true)
                CourseManager.getInstance(project).restart()
            }
            panel {
                row {
                    text(message("ui.OverviewView.invalidToken.title")).applyToComponent {
                        font = JBFont.h1()
                    }.comment(message("ui.OverviewView.invalidToken.description"))
                }
                with(tokenForm) {
                    token()
                    validation()
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private fun notEnrolledPanel(): DialogPanel {
        return panel {
            val courseName = CourseManager.getInstance(project).state.courseName
            panel {
                row {
                    text(message("ui.OverviewView.notEnrolled.title")).applyToComponent {
                        font = JBFont.h1()
                    }.comment(
                        message(
                            "ui.OverviewView.notEnrolled.description",
                            courseName ?: message("ui.OverviewView.notEnrolled.descriptionDefault")
                        )
                    )
                }
                row {
                    button(message("ui.OverviewView.refresh")) {
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
                    text(message("ui.OverviewView.notACourse.title")).applyToComponent {
                        font = JBFont.h1()
                    }.comment(message("ui.OverviewView.notACourse.description"))
                }
                row {
                    myActionLink(
                        message("ui.OverviewView.notACourse.createProject"),
                        CoursesIcons.LogoColor,
                        NewProjectAction()
                    )
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
    }

    private fun initializationIoErrorPanel(): DialogPanel {
        return panel {
            panel {
                row {
                    text(message("ui.OverviewView.initializationIoError.title")).applyToComponent {
                        font = JBFont.h1()
                    }.comment(message("ui.OverviewView.initializationIoError.description"))
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
        if (error != null) {
            return when (error) {
                CourseManager.Error.NETWORK_ERROR -> networkErrorPanel()
                CourseManager.Error.INVALID_TOKEN -> invalidTokenPanel()
                CourseManager.Error.NOT_ENROLLED -> notEnrolledPanel()
            }
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
                row { cell(BannerPanel(message("ui.BannerView.courseEnded"), BannerPanel.BannerType.ERROR)) }
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
                                text(message("ui.OverviewView.pointsCollected")).comment(grade?.let {
                                    message(
                                        "ui.OverviewView.grade",
                                        it
                                    )
                                })
                            }.topGap(TopGap.MEDIUM)
                            points.map { (category, points) ->
                                val categoryMaxPoints = maxPoints[category]!!
                                row {
                                    text(category).bold()
                                    cell(JProgressBar(0, categoryMaxPoints))
                                        .applyToComponent { value = points }
                                        .resizableColumn().align(AlignX.FILL)
                                    text("${points}/${categoryMaxPoints}").applyToComponent {
                                        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                                    }
                                }.layout(RowLayout.PARENT_GRID)
                            }
                            if (pointsUntilNextGrade != null && maxPointsOfNextGrade != null) {
                                row {
                                    text(message("ui.OverviewView.pointsUntilNextGrade"))
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
                            link(message("ui.OverviewView.pluginSettings")) {
                                ShowSettingsUtil.getInstance().showSettingsDialog(project, message("aplusCourses"))
                            }.applyToComponent {
                                icon = AllIcons.General.Settings
                                isFocusPainted = false
                            }
                        }
                        row {
                            browserLink(message("ui.OverviewView.coursePage"), course.htmlUrl).applyToComponent {
                                setIcon(CoursesIcons.LogoColor, atRight = false)
                                isFocusPainted = false
                            }
                        }
                    }
                }
            }.customize(UnscaledGaps(16, 32, 16, 32))
        }
        mainPanel.preferredSize = Dimension(mainPanelMaxWidth, mainPanel.preferredSize.height)
//        mainPanel.maximumSize = Dimension(mainPanelMaxWidth, Int.MAX_VALUE)
        return mainPanel
    }

    private class Grade(val grade: String?, val pointsUntilNext: Map<String, Int>?, val maxOfNext: Map<String, Int>?)

    private fun grade(grading: Grading, points: Map<String, Int>): Grade? {
        val style = grading.style
        val gradePoints = grading.points

        @NonNls val o1 = "o1"
        @NonNls val total = "total"

        return when (style) {
            o1 -> {
                @NonNls val a = "A"
                @NonNls val b = "B"
                @NonNls val c = "C"
                val aPoints = points[a] ?: return null
                var bPoints = points[b] ?: return null
                var cPoints = points[c] ?: return null

                // Calculate effective points for A and adjust B and C accordingly
                val requiredA = gradePoints.entries.last().value[a]!!
                val neededA = maxOf(0, requiredA - aPoints)
                val usedBForA = minOf(bPoints, neededA)
                val usedCForA = minOf(cPoints, neededA - usedBForA)
                val finalA = aPoints + usedBForA + usedCForA

                bPoints -= usedBForA
                cPoints -= usedCForA

                // Calculate effective points for B
                val requiredB = gradePoints.entries.last().value[b]!!
                val neededB = maxOf(0, requiredB - bPoints)
                val usedCForB = minOf(cPoints, neededB)
                val effectiveB = bPoints + usedCForB

                cPoints -= usedCForB

                // Determine the current grade based on effective points
                val currentGrade = gradePoints.entries.findLast { (_, requiredPoints) ->
                    finalA >= requiredPoints[a]!! &&
                            effectiveB >= requiredPoints[b]!! &&
                            cPoints >= requiredPoints[c]!!
                }

                // Determine the next achievable grade
                val nextGrade = gradePoints.entries.firstOrNull { (_, requiredPoints) ->
                    finalA < requiredPoints[a]!! ||
                            effectiveB < requiredPoints[b]!! ||
                            cPoints < requiredPoints[c]!!
                }

                // Calculate the points needed to reach the next grade
                val pointsToNextGrade = nextGrade?.value?.mapValues { (key, value) ->
                    maxOf(
                        0, value - when (key) {
                            a -> finalA
                            b -> effectiveB
                            c -> cPoints
                            else -> 0
                        }
                    )
                } ?: mapOf(a to 0, b to 0, c to 0)

                // Max points for the next grade
                val maxOfNext = mapOf(
                    a to (nextGrade?.value?.get(a) ?: 0),
                    b to (nextGrade?.value?.get(b) ?: 0),
                    c to (nextGrade?.value?.get(c) ?: 0)
                )

                Grade(currentGrade?.key, pointsToNextGrade, maxOfNext)
            }

            total -> {
                val totalPoints = points.values.sum()

                val currentGrade = gradePoints.entries.findLast { (_, requiredPoints) ->
                    totalPoints >= requiredPoints[total]!!
                }

                val nextGrade = gradePoints.entries.firstOrNull { (_, requiredPoints) ->
                    totalPoints < requiredPoints[total]!!
                }

                val pointsToNextGrade = if (nextGrade != null) {
                    mapOf(
                        total to maxOf(0, nextGrade.value[total]!! - totalPoints)
                    )
                } else {
                    mapOf(total to 0)
                }

                val maxOfNext = mapOf(total to (nextGrade?.value?.get(total) ?: 0))

                Grade(currentGrade?.key, pointsToNextGrade, maxOfNext)
            }

            else -> {
                null
            }
        }
    }

    private fun Panel.weekClosingTime(weeks: List<ExerciseGroup>) {
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
                text(message("ui.OverviewView.closing", weekNumber, formattedDateTime, howLongLeft)).applyToComponent {
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
        val loadedIcon = icon ?: url?.let {
            IconLoader.findIcon(URI(it).toURL(), true)
        }

        if (loadedIcon == null) {
            heightMultiplier = 0.0
        } else {
            image = IconLoader.toImage(loadedIcon, ScaleContext.createIdentity())
            heightMultiplier = loadedIcon.iconHeight.toDouble() / loadedIcon.iconWidth.toDouble()
            updateWidth(width)
        }
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

    override fun getWidth(): Int = width
    override fun getHeight(): Int = (width * heightMultiplier).toInt()

    override fun getPreferredSize(): Dimension = Dimension(width, height)
}