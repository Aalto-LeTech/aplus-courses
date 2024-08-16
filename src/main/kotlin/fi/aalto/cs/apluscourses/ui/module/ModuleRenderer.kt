package fi.aalto.cs.apluscourses.ui.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.AnActionLink
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.IntelliJSpacingConfiguration
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.utils.temp.DateDifferenceFormatter.formatTimeUntilNow
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.to

class ModuleRenderer(
    val module: Module,
    val index: Int,
    val project: Project,
) : JPanel(BorderLayout()) {
    private var panel: DialogPanel

    private val icon
        get() = if (module.category == Module.Category.AVAILABLE) CoursesIcons.ModuleDisabled
        else if (module.category == Module.Category.INSTALLED) CoursesIcons.Module
        else if (module.isUpdateAvailable) CoursesIcons.Info
        else CoursesIcons.NoPoints

    var isExpanded = false
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

    private fun Row.myLink(text: String, icon: Icon, action: (ActionEvent) -> Unit) =
        link(text, action).applyToComponent {
            setIcon(icon, false)
        }

    private fun Row.myActionLink(text: String, icon: Icon, action: AnAction) =
        cell(AnActionLink(text, action)).applyToComponent {
            setIcon(icon, false)
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
        get() = if (module.isUpdateAvailable) "Update Available"
        else "Dependencies Missing"


    private fun Panel.updateAvailable() {
        val metadata = module.metadata
        val changelog = module.changelog
        val firstRow = "Update available: ${metadata?.version} → ${module.latestVersion}"
        val text = if (changelog != null) "$firstRow<br><br>$changelog" else firstRow
        row {
            info(text).resizableColumn()
            button("Update") {
                println("Update ${module.name}")
                println("changedFiles = ${module.changedFiles()}")
                CourseManager.getInstance(project).updateModule(module)
            }.align(AlignY.BOTTOM)
        }
    }


    private fun Panel.dependenciesMissing() {
        val missingDependencies =
            CourseManager.getInstance(project).state.missingDependencies[module.name] ?: emptyList()
        row {
            info("Dependencies missing:")
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
    private fun zipSizeText(size: String) = "<span style=\"white-space: nowrap;\">Available at </span>" +
            "<span>${module.zipUrl} </span>" +
            "<span style=\"white-space: nowrap;\">(${size})</span>"

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
        row {
            info(zipSizeText.get())
                .bindText(zipSizeText)
                .resizableColumn()
            button("Install") {
                project.service<CourseManager>().installModule(module)
                installing.set(true)
            }.align(AlignY.BOTTOM).visibleIf(installing.not())
            button("Installing...") {}.applyToComponent {
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
            "Installed ${formatTimeUntilNow(installedTime)}"
        } else {
            "Metadata not found"
        }

        val nextExercise = ExercisesUpdaterService.getInstance(project).state.exerciseGroups
            .flatMap { group -> group.exercises.map { it to group } }
            .filter { it.first.module?.name == module.name }
            .firstOrNull { it.first.userPoints == 0 }

        val opener = project.service<Opener>()
        row { info(installedTimeText) }
        if (nextExercise != null) {
            val exercise = nextExercise.first
            val groupName = nextExercise.second.name
            row {
                info("Next assignment: $groupName")
            }
            row {
                myLink(exercise.name, CoursesIcons.NoSubmissions) {
                    opener.showExercise(exercise)
                }
            }
        }
        if (module.documentationExists) {
            row {
                myActionLink(
                    "Open documentation",
                    CoursesIcons.Docs,
                    opener.openDocumentationAction(module, "doc/index.html")
                )
            }
        }
        row {
            myLink("Show in project tree", AllIcons.General.Locate) {
                opener.showModuleInProjectTree(module)
            }
        }
    }

    private val rowBackground = if (index % 2 == 0) UIUtil.getTableBackground()
    else if (JBColor.isBright()) UIUtil.getTableBackground().brighter()
    else ColorUtil.darker(UIUtil.getTableBackground(), 1)
    private val hoverBackground = UIUtil.getTableBackground(true, false)
    private var isHovering = false

    init {
//        updateZipSize("??? ??")
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