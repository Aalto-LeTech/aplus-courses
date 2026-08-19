package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.components.service
import fi.aalto.cs.apluscourses.services.Plugins
import com.intellij.icons.AllIcons
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.util.application
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.services.course.SetupAction
import fi.aalto.cs.apluscourses.ui.tabularFont
import fi.aalto.cs.apluscourses.services.course.SetupStep
import fi.aalto.cs.apluscourses.services.course.SetupStepState
import fi.aalto.cs.apluscourses.utils.RequiredPluginsFile
import org.jetbrains.annotations.NonNls
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Icon
import javax.swing.JPanel

/**
 * The list of things that still have to happen before the course is ready.
 */
class SetupChecklist(private val project: Project) : JPanel(GridBagLayout()) {
    private var stepIds: List<String> = emptyList()
    private var rows: Map<String, ChecklistRow> = emptyMap()

    init {
        isOpaque = false
    }

    fun update(steps: List<SetupStep>) {
        val ids = steps.map { it.id }
        if (ids != stepIds) {
            stepIds = ids
            rebuild(steps)
        }
        steps.forEach { rows[it.id]?.set(it) }
    }

    private fun rebuild(steps: List<SetupStep>) {
        removeAll()
        rows = steps.associate { it.id to ChecklistRow(project) }
        steps.forEachIndexed { index, step -> rows.getValue(step.id).addTo(this, index) }
        revalidate()
        repaint()
    }
}

private class ChecklistRow(private val project: Project) {
    private val icon = JBLabel()
    private val title = JBLabel()
    private val detail = JBLabel().apply {
        foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
    }

    private val proseFont = detail.font

    private var pending: SetupAction? = null

    private val action = ActionLink().apply {
        isVisible = false
        addActionListener { pending?.let(::perform) }
    }

    fun set(step: SetupStep) {
        icon.icon = iconFor(step.state)
        title.text = step.title
        title.foreground =
            if (step.state == SetupStepState.PENDING) JBUI.CurrentTheme.ContextHelp.FOREGROUND
            else JBUI.CurrentTheme.Label.foreground()
        detail.text = step.detail.orEmpty()
        // Tabular digits for measurements, so a changing counter keeps a constant width.
        detail.font = if (step.detailIsMeasurement) tabularFont() else proseFont
        setAction(step.action)
    }

    private fun setAction(newAction: SetupAction?) {
        pending = newAction
        action.isVisible = newAction != null
        action.text = when (newAction) {
            SetupAction.INSTALL_PLUGINS -> message("ui.setup.installPlugins")
            SetupAction.OPEN_PLUGIN_SETTINGS -> message("ui.setup.openPluginSettings")
            SetupAction.RESTART_IDE -> message("ui.setup.restartIde")
            null -> ""
        }
    }

    private fun perform(toDo: SetupAction) {
        when (toDo) {
            SetupAction.INSTALL_PLUGINS -> {
                val required = RequiredPluginsFile.read(project)
                    .mapTo(mutableSetOf()) { PluginId.getId(it) }
                application.service<Plugins>().installMissing(project, required)
            }

            SetupAction.OPEN_PLUGIN_SETTINGS ->
                ShowSettingsUtil.getInstance().showSettingsDialog(project, PLUGINS_PAGE)

            SetupAction.RESTART_IDE -> application.invokeLater {
                (application as ApplicationEx).restart(true)
            }
        }
    }

    private fun iconFor(state: SetupStepState): Icon = when (state) {
        SetupStepState.PENDING -> AllIcons.General.TodoDefault
        SetupStepState.RUNNING -> AnimatedIcon.Default.INSTANCE
        SetupStepState.DONE -> AllIcons.General.InspectionsOK
        SetupStepState.ACTION_NEEDED -> AllIcons.General.Warning
        SetupStepState.FAILED -> AllIcons.General.Error
    }

    fun addTo(table: JPanel, row: Int) {
        val gap = JBUI.scale(VERTICAL_GAP)
        fun place(component: Component, column: Int, stretch: Boolean) {
            table.add(component, GridBagConstraints().apply {
                gridx = column; gridy = row
                anchor = GridBagConstraints.LINE_START
                if (stretch) {
                    weightx = 1.0
                    fill = GridBagConstraints.HORIZONTAL
                }
                insets = JBUI.insets(gap, 0, gap, JBUI.scale(COLUMN_GAP))
            })
        }
        place(icon, 0, stretch = false)
        place(title, 1, stretch = false)
        place(detail, 2, stretch = true)
        place(action, 3, stretch = false)
    }
}

/** The plugin manager, addressed by display name as the settings dialog expects. */
@NonNls
private const val PLUGINS_PAGE = "Plugins"

private const val COLUMN_GAP = 6
private const val VERTICAL_GAP = 2
