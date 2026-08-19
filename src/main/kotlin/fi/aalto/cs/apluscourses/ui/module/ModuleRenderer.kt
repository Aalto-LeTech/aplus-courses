package fi.aalto.cs.apluscourses.ui.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.hover.HoverListener
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.ui.PendingValueLabel
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.ui.Utils.myActionLink
import fi.aalto.cs.apluscourses.ui.Utils.myLink
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesTreeRenderer.Companion.exerciseIcon
import fi.aalto.cs.apluscourses.utils.DateDifferenceFormatter.formatTimeUntilNow
import org.jetbrains.annotations.TestOnly
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import kotlinx.coroutines.Dispatchers
import java.awt.Component as AwtComponent
import kotlinx.coroutines.withContext

class ModuleRenderer(
    val module: Module,
    var index: Int,
    val project: Project,
    private val onToggle: (ModuleRenderer) -> Unit,
) : JPanel(BorderLayout()) {
    private var panel: DialogPanel

    val isShowingInstalling: Boolean
        get() = module.status == Component.Status.LOADING

    private val icon
        get() = when {
            isShowingInstalling -> CoursesIcons.Loading
            module.category == Module.Category.AVAILABLE -> CoursesIcons.ModuleDisabled
            module.category == Module.Category.INSTALLED -> CoursesIcons.Module
            module.isUpdateAvailable -> CoursesIcons.Info
            else -> CoursesIcons.NoPoints
        }

    private var iconLabel: JLabel? = null

    @RequiresEdt
    fun refreshIcon() {
        installing.set(isShowingInstalling)
        iconLabel?.icon = icon
    }

    var isExpanded: Boolean = false
        private set

    private fun Row.info(text: String) =
        text(text).applyToComponent {
            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            addMouseListener(clickListener)
        }

    private fun Panel.header() {
        row {
            icon(icon).gap(RightGap.SMALL).applyToComponent { iconLabel = this }
            label(module.name).gap(RightGap.SMALL)
            label(module.language ?: "").applyToComponent {
                foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            }.resizableColumn()
            if (module.category == Module.Category.ACTION_REQUIRED) {
                label(actionRequiredText).gap(RightGap.SMALL).applyToComponent {
                    foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                }
            }
            icon(if (isExpanded) AllIcons.General.ChevronUp else AllIcons.General.ChevronDown)
        }
    }

    private val actionRequiredText
        get() = if (module.isUpdateAvailable) message("ui.ModuleRenderer.actionRequired.update")
        else message("ui.ModuleRenderer.actionRequired.dependenciesMissing")


    private fun Panel.updateAvailable() {
        val metadata = module.metadata
        val changelog = module.changelog
        val firstRow =
            message("ui.ModuleRenderer.updateAvailable.title", metadata?.version ?: "???", module.latestVersion)
        val text = if (changelog != null) "$firstRow<br><br>$changelog" else firstRow
        row {
            info(text).resizableColumn()
            button(message("ui.ModuleRenderer.updateAvailable.button")) {
                CourseManager.getInstance(project).updateModule(module)
            }.align(AlignY.BOTTOM)
        }
    }


    private fun Panel.dependenciesMissing() {
        val missingDependencies =
            CourseManager.getInstance(project).state.missingDependencies[module.name] ?: emptyList()
        row {
            info(message("ui.ModuleRenderer.dependenciesMissing.title"))
        }
        missingDependencies.forEach { component ->
            row {
                if (component is Module) {
                    myLink(component.name, CoursesIcons.ModuleDisabled) {
                        project.service<Opener>().showModule(component)
                    }
                } else {
                    info(component.name)
                }
            }
        }
    }

    private val installing = AtomicBooleanProperty(isShowingInstalling)

    val boundInstallingValue: Boolean
        @TestOnly get() = installing.get()
    private var isZipSizeSet = false

    private val zipSize = PendingValueLabel(
        placeholderWidth = ZIP_SIZE_PLACEHOLDER_WIDTH,
        prefix = message("ui.ModuleRenderer.available.fileSize") + " ",
        textColor = JBUI.CurrentTheme.ContextHelp.FOREGROUND,
        tabularValue = true,
    )


    private fun Panel.available() {
        row {
            cell(zipSize).resizableColumn()
            button(message("ui.ModuleRenderer.available.button")) {
                project.service<CourseManager>().installModule(module)
                refreshIcon()
            }.align(AlignY.BOTTOM).visibleIf(installing.not())
            button(message("ui.ModuleRenderer.available.installing")) {}.applyToComponent {
                isEnabled = false
            }.align(AlignY.BOTTOM).visibleIf(installing)
        }
        if (!isZipSizeSet) {
            CoursesClient.getInstance(project).execute {
                val size = it.getFileSize(module.zipUrl) ?: return@execute
                withContext(Dispatchers.EDT) {
                    zipSize.setValue(StringUtil.formatFileSize(size))
                    isZipSizeSet = true
                }
            }
        }
    }


    private fun Panel.installed() {
        val installedTime = module.metadata?.downloadedAt
        val installedTimeText = if (installedTime != null) {
            message("ui.ModuleRenderer.installed.title", formatTimeUntilNow(installedTime))
        } else {
            message("ui.ModuleRenderer.installed.metadataError")
        }

        val nextExercise = ExercisesUpdater.getInstance(project).state.exerciseGroups
            .firstNotNullOfOrNull { group ->
                group.exercises
                    .firstOrNull { it.module?.name == module.name && it.userPoints == 0 }
                    ?.let { it to group }
            }

        val opener = project.service<Opener>()
        row { info(installedTimeText) }
        if (nextExercise != null) {
            val (exercise, group) = nextExercise
            row {
                info(message("ui.ModuleRenderer.installed.nextAssignment", group.name))
            }
            row {
                myLink(exercise.name, exerciseIcon(exercise)) {
                    opener.showExercise(exercise)
                }
            }
        }
        if (module.documentationExists) {
            row {
                myActionLink(
                    message("ui.ModuleRenderer.installed.documentation"),
                    CoursesIcons.Docs,
                    opener.openDocumentationAction(module, "doc/index.html")
                )
            }
        }
        row {
            myLink(message("ui.ModuleRenderer.installed.showInProject"), AllIcons.General.Locate) {
                opener.showModuleInProjectTree(module)
            }
        }
    }

    private val rowBackground
        get() =
            if (index % 2 == 0) UIUtil.getTableBackground()
            else if (JBColor.isBright()) UIUtil.getTableBackground().brighter()
            else ColorUtil.darker(UIUtil.getTableBackground(), 1)
    private val hoverBackground = UIUtil.getTableBackground(true, false)
    private var isHovering = false

    private val clickListener = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) = onToggle(this@ModuleRenderer)
    }

    init {
        this.panel = base { header() }
        add(panel, BorderLayout.CENTER)
        addMouseListener(clickListener)
        trackHover()
    }

    private fun trackHover() {
        object : HoverListener() {
            override fun mouseEntered(component: AwtComponent, x: Int, y: Int) = updateBackground(true)
            override fun mouseMoved(component: AwtComponent, x: Int, y: Int) = Unit
            override fun mouseExited(component: AwtComponent) = updateBackground(false)
        }.addTo(this)
    }

    private fun base(init: Panel.() -> Unit) = panel {
        panel {
            customizeSpacingConfiguration(object : IntelliJSpacingConfiguration() {
                override val verticalComponentGap: Int = 1
            }) {
                init()
            }
        }.customize(UnscaledGaps(3, 8, 3, 8)).apply {
            isOpaque = false
        }
    }.apply {
        background = if (isHovering) hoverBackground else rowBackground
        addMouseListener(clickListener)
    }

    fun collapse() {
        if (!isExpanded) return
        isExpanded = false
        updatePanel {
            header()
        }
    }

    fun expand() {
        isExpanded = true
        updatePanel {
            header()
            when {
                module.category == Module.Category.AVAILABLE -> available()
                module.category == Module.Category.INSTALLED -> installed()
                module.isUpdateAvailable -> updateAvailable()
                else -> dependenciesMissing()
            }
        }
    }

    @RequiresEdt
    private fun updatePanel(init: Panel.() -> Unit) {
        remove(panel)
        panel = base {
            init()
        }
        add(panel, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun updateBackground(isHovering: Boolean) {
        this.isHovering = isHovering
        panel.background = if (isHovering) hoverBackground else rowBackground
    }

    fun refreshBackground(): Unit = updateBackground(isHovering)

    private companion object {
        const val ZIP_SIZE_PLACEHOLDER_WIDTH = 46
    }
}
