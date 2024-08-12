package fi.aalto.cs.apluscourses.ui.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.actionButton
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.utils.temp.DateDifferenceFormatter.formatTimeUntilNow
import icons.PluginIcons
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import kotlin.to

class ModuleRenderer(
    val module: Module,
    val index: Int,
    val project: Project,
    val collapseAll: () -> Unit
) : JPanel(BorderLayout()) {
    private var panel: DialogPanel

    private val icon
        get() = if (module.category == Module.Category.AVAILABLE) PluginIcons.A_PLUS_MODULE_DISABLED
        else if (module.category == Module.Category.INSTALLED) PluginIcons.A_PLUS_MODULE
        else if (module.isUpdateAvailable) PluginIcons.A_PLUS_INFO
        else PluginIcons.A_PLUS_NO_POINTS

    var isExpanded = false
        private set

    private fun mouseListener(passClick: Boolean = false) = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) = if (passClick) this@ModuleRenderer.dispatchEvent(e) else {
        }

        override fun mouseMoved(e: MouseEvent) = this@ModuleRenderer.dispatchEvent(e)

        //        override fun mouseEntered(e: MouseEvent) = this@ModuleRenderer.dispatchEvent(e)
//        override fun mouseExited(e: MouseEvent) =
//            this@ModuleRenderer.dispatchEvent(e)
    }

    private fun Row.info(text: String) =
        text(text).applyToComponent {
            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            addMouseListener(mouseListener(true))
            addMouseMotionListener(mouseListener())
        }

    private fun Row.myLink(text: String, action: (ActionEvent) -> Unit) =
        link(text, action).applyToComponent {
            addMouseListener(mouseListener())
            addMouseMotionListener(mouseListener())
        }

    private fun Row.myActionButton(action: AnAction) =
        actionButton(action).applyToComponent {
            addMouseListener(mouseListener())
            addMouseMotionListener(mouseListener())
        }

    private fun Panel.header() {
        row {
            icon(icon).gap(RightGap.SMALL)
            label(module.name).resizableColumn()
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
        val missingDependencies = CourseManager.getInstance(project).state.missingDependencies[module.name]
        row {
            info("Dependencies missing: ${missingDependencies?.joinToString(", ")}")
                .resizableColumn()
        }
    }

    private fun Panel.available() {
        val url = module.zipUrl
        row {
            info("<span style=\"white-space: nowrap;\">Available at </span><span>${url}</span><span style=\"white-space: nowrap;\">(????)</span>")
                .resizableColumn()
            button("Install") {
                project.service<CourseManager>().installModule(module)
            }.align(AlignY.BOTTOM)
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
        row {
            info(installedTimeText)
            myActionButton(opener.showModuleInProjectTreeAction(module)).gap(RightGap.SMALL)
            if (module.documentationExists) {
                myActionButton(opener.openDocumentationAction(module, "doc/index.html"))
            }
        }
        if (nextExercise != null) {
            val exercise = nextExercise.first
            val groupName = nextExercise.second.name
            row {
                info("Next assignment: ${groupName}")
            }
            row {
                myLink(exercise.name) {
                    opener.showExercise(exercise)
                }.applyToComponent {
                    setIcon(PluginIcons.A_PLUS_NO_SUBMISSIONS, false)
                }
            }
        }
    }

    private val rowBackground = if (index % 2 == 0) UIUtil.getTableBackground()
    else if (JBColor.isBright()) UIUtil.getTableBackground().brighter()
    else ColorUtil.darker(UIUtil.getTableBackground(), 1)
    private val hoverBackground = UIUtil.getTableBackground(true, false)
    private var isHovering = false

    init {
        this.panel = base {
            header()
        }
        add(panel, BorderLayout.CENTER)
//        addMouseListener(object : MouseAdapter() {
//            override fun mousePressed(e: MouseEvent) {
//                val isOpen = isExpanded
//                collapseAll()
//                if (!isOpen) {
//                    expand()
//                }
//            }
//
//            override fun mouseEntered(e: MouseEvent) {
//                isHovering = true
//                panel.background = hoverBackground
//            }
//
//            override fun mouseExited(e: MouseEvent) {
//                isHovering = false
//                panel.background = rowBackground
//            }
//
//            override fun mouseMoved(e: MouseEvent?) {
//                println("Moved")
//            }
//        })
    }

    private fun base(init: Panel.() -> Unit) = panel {
        panel {
            init()
        }.customize(UnscaledGaps(0, 8, 0, 8)).apply {
            isOpaque = false
        }
    }.apply {
        background = if (isHovering) hoverBackground else rowBackground
    }

    fun collapse() {
//        val mouseY = parent.mousePosition.y
//        if (this.y < mouseY && mouseY < this.y + 30) {
//            panel.background = hoverBackground
//        } else {
//            panel.background = rowBackground
//        }
//        application.invokeLater {
//            isHovering = this.isComponentUnderMouse()
//        }
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

    private fun updatePanel(init: Panel.() -> Unit) {
        remove(panel)
        panel = base {
            init()
        }
        add(panel, BorderLayout.CENTER)
        revalidate()
        repaint()
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