package fi.aalto.cs.apluscourses.ui.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.ui.Utils.myActionLink
import fi.aalto.cs.apluscourses.ui.Utils.myLink
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesTreeRenderer.Companion.exerciseIcon
import fi.aalto.cs.apluscourses.utils.DateDifferenceFormatter.formatTimeUntilNow
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class ModuleRenderer(
    val module: Module,
    var index: Int,
    val project: Project,
) : JPanel(BorderLayout()) {
    private var panel: DialogPanel

    private val icon
        get() = if (module.category == Module.Category.AVAILABLE) CoursesIcons.ModuleDisabled
        else if (module.category == Module.Category.INSTALLED) CoursesIcons.Module
        else if (module.isUpdateAvailable) CoursesIcons.Info
        else CoursesIcons.NoPoints

    var isExpanded: Boolean = false
        private set

    private fun Row.info(text: String) =
        text(text).applyToComponent {
            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            addMouseListener(object : MouseAdapter() { // Forward mouse clicks to the parent component
                override fun mousePressed(e: MouseEvent) {
                    val convertedPoint =
                        SwingUtilities.convertPoint(this@applyToComponent, e.point, this@ModuleRenderer.parent)
                    e.translatePoint(convertedPoint.x - e.x, convertedPoint.y - e.y)
                    this@ModuleRenderer.parent.dispatchEvent(e)
                }
            })
        }

    private fun Panel.header() {
        row {
            icon(icon).gap(RightGap.SMALL)
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


    private val installing = AtomicBooleanProperty(false)
    private var isZipSizeSet = false
    private fun zipSizeText(size: String) = message("ui.ModuleRenderer.available.fileSize", size)

    private val zipSizeText = AtomicProperty<String>(zipSizeText("??? ??"))


    @NonNls
    fun formatFileSize(sizeInBytes: Long): String {
        val sizeInKB = sizeInBytes / 1024.0
        val sizeInMB = sizeInKB / 1024.0

        return when {
            sizeInMB >= 1 -> "%.2f MB".format(sizeInMB)
            sizeInKB >= 1 -> "%.2f KB".format(sizeInKB)
            else -> "$sizeInBytes B"
        }
    }

    private fun Panel.available() {
        if (module.status == Component.Status.LOADING) {
            installing.set(true)
        }
        row {
            info(zipSizeText.get())
                .bindText(zipSizeText)
                .resizableColumn()
            button(message("ui.ModuleRenderer.available.button")) {
                project.service<CourseManager>().installModule(module)
                installing.set(true)
            }.align(AlignY.BOTTOM).visibleIf(installing.not())
            button(message("ui.ModuleRenderer.available.installing")) {}.applyToComponent {
                isEnabled = false
            }.align(AlignY.BOTTOM).visibleIf(installing)
        }
        if (!isZipSizeSet) {
            CoursesClient.getInstance(project).execute {
                val size = it.getFileSize(module.zipUrl) ?: return@execute
                zipSizeText.set(zipSizeText(formatFileSize(size)))
                isZipSizeSet = true
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
            .flatMap { group -> group.exercises.map { it to group } }
            .filter { it.first.module?.name == module.name }
            .firstOrNull { it.first.userPoints == 0 }

        val opener = project.service<Opener>()
        row { info(installedTimeText) }
        if (nextExercise != null) {
            val exercise = nextExercise.first
            val groupName = nextExercise.second.name
            row {
                info(message("ui.ModuleRenderer.installed.nextAssignment", groupName))
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

    init {
        this.panel = base { header() }
        add(panel, BorderLayout.CENTER)
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
        updateBackground(true)
    }

    private fun updatePanel(init: Panel.() -> Unit) {
        remove(panel)
        panel = base {
            init()
        }
        application.invokeLater {
            add(panel, BorderLayout.CENTER)
            revalidate()
            repaint()
        }
    }

    fun updateBackground(isHovering: Boolean) {
        if (isHovering) {
            panel.background = hoverBackground
        } else {
            panel.background = rowBackground
        }
    }

    fun setVisibility(visible: Boolean) {
        isVisible = visible
    }
}