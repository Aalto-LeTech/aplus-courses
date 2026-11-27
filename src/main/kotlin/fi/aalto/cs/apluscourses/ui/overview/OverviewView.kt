package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.icons.AllIcons
import com.intellij.ide.actions.NewProjectAction
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.Background
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.CourseNews
import fi.aalto.cs.apluscourses.services.course.SetupStepState
import fi.aalto.cs.apluscourses.ui.*
import fi.aalto.cs.apluscourses.ui.Utils.myActionLink
import fi.aalto.cs.apluscourses.ui.news.NewsPanel
import fi.aalto.cs.apluscourses.utils.DateDifferenceFormatter
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

class OverviewView(private val project: Project) : SimpleToolWindowPanel(true, true) {
    private val courseName = AtomicProperty<String?>(null)
    private val userName = AtomicProperty<String?>(null)
    private val courseHasEnded = AtomicBooleanProperty(false)
    private val gradeText = AtomicProperty("")
    private val gradeKnown = AtomicBooleanProperty(false)
    private val showsGradeProgression = AtomicBooleanProperty(false)
    private val closingText = AtomicProperty<String?>(null)
    private val closingVisible = AtomicBooleanProperty(false)
    private val authTitle = AtomicProperty("")
    private val notEnrolledDescription = AtomicProperty("")
    private val networkErrorDetails = AtomicProperty("")
    private val initErrorDetails = AtomicProperty("")
    private val errorDetailsVisible = AtomicBooleanProperty(false)

    private val courseFiles = CourseFileManager.getInstance(project)

    private val banner = ResponsiveImagePanel(
        width = MAIN_PANEL_MAX_WIDTH,
        maxWidth = MAIN_PANEL_MAX_WIDTH,
        scope = project.service<Background>().cs,
        initialAspectRatio = courseFiles.rememberedBannerAspectRatio(),
        // Store the image size. The next start reads it back as the initial aspect ratio.
        onImageResolved = { imageWidth, imageHeight ->
            courseFiles.state.bannerImageWidth = imageWidth
            courseFiles.state.bannerImageHeight = imageHeight
        }
    )

    private val setupChecklist = SetupChecklist(project)
    private val setupTitle = AtomicProperty("")
    private val setupVisible = AtomicBooleanProperty(false)

    private val collectedTable = PointsTable()
    private val nextGradeTable = PointsTable()
    private val coursePageLink =
        PendingLink(LINK_WIDTH, message("ui.OverviewView.coursePage"), AllIcons.General.Web)

    private val newsPanel = NewsPanel(
        CoroutineSettleScheduler(project.service<Background>().cs),
        onSizeChanged = ::newsSizeChanged
    ) { CourseNews.getInstance(project).setAllRead() }
    private val newsVisible = AtomicBooleanProperty(false)

    private val authForm = TokenForm(project) { restart() }
    private val invalidTokenForm = TokenForm(project) { restart() }

    private val mainPanel: DialogPanel = panel {
        row { cell(banner) }
        row {
            cell(BannerPanel(message("ui.BannerView.courseEnded"), BannerPanel.BannerType.ERROR))
        }.visibleIf(courseHasEnded)
        panel {
            customizeSpacingConfiguration(object : IntelliJSpacingConfiguration() {
                override val verticalComponentGap: Int = 1
            }) {
                row {
                    panel {
                        row { pendingText(courseName, COURSE_NAME_WIDTH, JBFont.h0()) }
                        row { pendingText(userName, USER_NAME_WIDTH) }
                    }.resizableColumn()
                    // Keeping links beside the heading leaves more vertical room for news.
                    panel {
                        row {
                            link(message("ui.OverviewView.pluginSettings")) {
                                ShowSettingsUtil.getInstance()
                                    .showSettingsDialog(project, message("aplusCourses"))
                            }.applyToComponent {
                                icon = AllIcons.General.Settings
                                isFocusPainted = false
                            }
                        }
                        // The address is not known until the course arrives, so the row holds a
                        // placeholder link.
                        row { cell(coursePageLink) }
                    }.align(AlignY.TOP)
                }.topGap(TopGap.SMALL)

                row { text("").bindText(setupTitle) }.topGap(TopGap.MEDIUM).visibleIf(setupVisible)
                row { cell(setupChecklist).resizableColumn().align(AlignX.FILL) }
                    .visibleIf(setupVisible)

                group(indent = false) {
                    row { text(message("ui.OverviewView.pointsCollected")) }.topGap(TopGap.MEDIUM)
                    row { commentText(gradeText) }.visibleIf(gradeKnown)

                    row { cell(collectedTable).resizableColumn().align(AlignX.FILL) }

                    // Reserved only for a grading style that produces a progression. Without one
                    // the section has no data.
                    row { text(message("ui.OverviewView.pointsUntilNextGrade")) }
                        .topGap(TopGap.SMALL)
                        .visibleIf(showsGradeProgression)
                    row { cell(nextGradeTable).resizableColumn().align(AlignX.FILL) }
                        .visibleIf(showsGradeProgression)

                    separator().topGap(TopGap.MEDIUM).bottomGap(BottomGap.MEDIUM)

                    row {
                        pendingText(
                            closingText,
                            CLOSING_WIDTH,
                            JBFont.medium(),
                            JBUI.CurrentTheme.ContextHelp.FOREGROUND
                        )
                    }.bottomGap(BottomGap.SMALL).visibleIf(closingVisible)

                    separator().topGap(TopGap.MEDIUM).bottomGap(BottomGap.MEDIUM)
                        .visibleIf(newsVisible)
                    row {
                        text(message("ui.OverviewView.news")).applyToComponent {
                            font = JBFont.h2().asBold()
                        }
                    }.bottomGap(BottomGap.SMALL).visibleIf(newsVisible)
                    row { cell(newsPanel).resizableColumn().align(AlignX.FILL) }
                        .visibleIf(newsVisible)
                }
            }
        }.customize(UnscaledGaps(16, 32, 16, 32))
    }

    private val mainScreen = WidthLimited(MAIN_PANEL_MAX_WIDTH, mainPanel)

    private val screens: Map<Screen, JComponent> = mapOf(
        Screen.NOT_A_COURSE to statusPanel(
            messageProperty("ui.OverviewView.notACourse.title"),
            messageProperty("ui.OverviewView.notACourse.description")
        ) {
            row {
                myActionLink(
                    message("ui.OverviewView.notACourse.createProject"),
                    CoursesIcons.LogoColor,
                    NewProjectAction()
                )
            }
        },
        Screen.IO_ERROR to statusPanel(
            messageProperty("ui.OverviewView.initializationIoError.title"),
            messageProperty("ui.OverviewView.initializationIoError.description")
        ) {
            retryButton(initErrorDetails) {
                CourseManager.getInstance(project).retryInitialization()
                update()
            }
        },
        Screen.AUTH to statusPanel(authTitle, messageProperty("ui.OverviewView.auth.description")) {
            with(authForm) {
                token(compact = true)
                validation()
            }
        },
        Screen.NETWORK_ERROR to statusPanel(
            messageProperty("ui.OverviewView.networkError.title"),
            messageProperty("ui.OverviewView.networkError.description")
        ) { retryButton(networkErrorDetails) },
        Screen.INVALID_TOKEN to statusPanel(
            messageProperty("ui.OverviewView.invalidToken.title"),
            messageProperty("ui.OverviewView.invalidToken.description")
        ) {
            with(invalidTokenForm) {
                token(compact = true)
                validation()
            }
        },
        Screen.NOT_ENROLLED to statusPanel(
            messageProperty("ui.OverviewView.notEnrolled.title"),
            notEnrolledDescription
        ) { retryButton() },
        Screen.MAIN to mainScreen
    )

    private var currentScreen: Screen? = null

    private fun allowsHorizontalScroll(screen: Screen?): Boolean = screen == Screen.MAIN

    private val screenContainer = ScrollableContent { !allowsHorizontalScroll(currentScreen) }
        .apply { isOpaque = false }

    private val scrollPane = JBScrollPane(screenContainer).apply {
        verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
    }

    init {
        toolbar = null
        setContent(scrollPane)
        update()
    }

    fun update() {
        val model = readOverviewModel(project)
        showScreen(model.screen)

        authForm.refresh()
        invalidTokenForm.refresh()

        networkErrorDetails.set(model.errorDetails ?: "")
        initErrorDetails.set(model.errorDetails ?: "")
        errorDetailsVisible.set(model.errorDetails != null)
        authTitle.set(message("ui.OverviewView.auth.title", model.configuredCourseName ?: ""))
        notEnrolledDescription.set(
            message(
                "ui.OverviewView.notEnrolled.description",
                model.configuredCourseName
                    ?: message("ui.OverviewView.notEnrolled.descriptionDefault")
            )
        )
        if (model.screen == Screen.MAIN) applyToMainPanel(model)
    }

    private fun applyToMainPanel(model: OverviewModel) {
        banner.setUrl(model.bannerUrl)
        courseName.set(model.courseName)
        userName.set(model.userName)
        courseHasEnded.set(model.courseHasEnded)
        coursePageLink.setUrl(model.coursePageUrl)

        gradeKnown.set(model.grade != null)
        model.grade?.let { gradeText.set(message("ui.OverviewView.grade", it)) }

        setupVisible.set(model.setupSteps.isNotEmpty())
        if (model.setupSteps.isNotEmpty()) {
            val stillSettingUp = model.setupSteps.any { it.state != SetupStepState.ACTION_NEEDED }
            setupTitle.set(
                if (stillSettingUp && model.courseName != null) {
                    message("ui.setup.title", model.courseName)
                } else {
                    message("ui.setup.actionNeeded")
                }
            )
            setupChecklist.update(model.setupSteps)
        }

        val categories = model.categories.orEmpty()
        collectedTable.update(
            model.categories,
            categories.associateWith { Points.Collected(model.collected?.get(it), model.maxPoints?.get(it)) }
        )
        showsGradeProgression.set(model.showsGradeProgression)
        nextGradeTable.update(
            model.nextGradeCategories,
            model.nextGradeCategories.orEmpty().associateWith { model.nextGradeProgress(it) }
        )

        val scoreWidth = maxOf(collectedTable.naturalScoreWidth(), nextGradeTable.naturalScoreWidth())
        collectedTable.pinScoreWidth(scoreWidth)
        nextGradeTable.pinScoreWidth(scoreWidth)

        closingVisible.set(model.showsClosingWeek())
        closingText.set(model.closingWeek?.let { closingMessage(it) })

        newsPanel.update(model.news)
        newsVisible.set(model.news.isNotEmpty())

        // Hidden views may have invalid ancestors that stop invalidation propagation.
        mainScreen.forgetSize()
    }

    /** Screens retain their components so a refresh preserves token input. */
    private fun showScreen(screen: Screen) {
        if (screen == currentScreen) return
        currentScreen = screen
        screenContainer.removeAll()
        screenContainer.add(screens.getValue(screen), BorderLayout.CENTER)
        scrollPane.horizontalScrollBarPolicy =
            if (allowsHorizontalScroll(screen)) ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            else ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        screenContainer.revalidate()
        screenContainer.repaint()
    }

    private fun statusPanel(
        title: ObservableMutableProperty<String>,
        description: ObservableMutableProperty<String>,
        action: (Panel.() -> Unit)? = null
    ): DialogPanel = panel {
        panel {
            row { text("").bindText(title).applyToComponent { font = JBFont.h1() } }
            row { commentText(description) }
            action?.invoke(this)
        }.customize(UnscaledGaps(16, 32, 16, 32))
    }

    private fun restart() {
        CourseManager.getInstance(project).restart()
        update()
    }

    private fun Panel.retryButton(
        details: ObservableMutableProperty<String>? = null,
        action: () -> Unit = ::restart
    ) {
        details?.let { row { commentText(it) }.visibleIf(errorDetailsVisible) }
        row {
            button(message("ui.OverviewView.refresh")) { action() }
        }
    }

    private var newsRelayoutPending = false

    private fun newsSizeChanged() {
        if (newsRelayoutPending) return
        newsRelayoutPending = true
        // Wrapping runs during layout. Invalidate after that pass marks its ancestors valid.
        application.invokeLater {
            newsRelayoutPending = false
            if (project.isDisposed) return@invokeLater
            mainPanel.invalidate()
            mainScreen.invalidate()
            screenContainer.invalidate()
            scrollPane.revalidate()
        }
    }

    /** Scrolls to the news section after a notification opens the overview. */
    fun showNews() {
        if (!newsVisible.get()) return
        newsPanel.scrollRectToVisible(Rectangle(0, 0, newsPanel.width, 1))
    }

    private var oldWidth = 0

    override fun getWidth(): Int {
        val newWidth = super.getWidth()

        if (newWidth != oldWidth && this.isShowing) {
            oldWidth = newWidth
            banner.updateWidth(newWidth)
        }
        return newWidth
    }
}

/** Bound labels use the IDE context-help style because their text changes after loading. */
private fun Row.commentText(text: ObservableMutableProperty<String>) {
    this.text("").bindText(text).applyToComponent {
        font = JBFont.medium()
        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
    }
}

private fun messageProperty(key: String): ObservableMutableProperty<String> = AtomicProperty(message(key))

private fun OverviewModel.nextGradeProgress(category: String): Points =
    Points.Remaining(
        untilNext = pointsUntilNextGrade?.get(category),
        maxOfNext = maxPointsOfNextGrade?.get(category)
    )

private fun closingMessage(week: ClosingWeek): String {
    val localDateTime = week.closingTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val formattedDateTime = localDateTime.run {
        "${day}.${month.number}.${year} ${hour.toString().padStart(2, '0')}:${
            minute.toString().padStart(2, '0')
        }"
    }
    return message(
        "ui.OverviewView.closing",
        week.number,
        formattedDateTime,
        DateDifferenceFormatter.formatTimeSinceNow(week.closingTime)
    )
}

private class WidthLimited(private val maxWidth: Int, content: JComponent) : JPanel(BorderLayout()) {
    private var cachedPreferredSize: Dimension? = null

    init {
        isOpaque = false
        add(content, BorderLayout.CENTER)
    }

    /**
     * The width never depends on the room available, so the height is worth keeping until
     * something in the content changes. Measuring it again means re-wrapping every text.
     */
    override fun getPreferredSize(): Dimension = cachedPreferredSize
        ?: Dimension(maxWidth, super.getPreferredSize().height).also { cachedPreferredSize = it }

    /** Invalidation only travels through valid ancestors, so a content change says so directly. */
    fun forgetSize() {
        cachedPreferredSize = null
    }

    override fun invalidate() {
        forgetSize()
        super.invalidate()
    }
}

private const val MAIN_PANEL_MAX_WIDTH = 300
