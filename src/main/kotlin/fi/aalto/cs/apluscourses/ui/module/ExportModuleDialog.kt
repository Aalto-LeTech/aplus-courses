package fi.aalto.cs.apluscourses.ui.module

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.Module
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.whenPropertyChanged
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.model.people.User
import java.awt.Component
import java.nio.file.Path
import javax.swing.JList
import javax.swing.ListCellRenderer

class ExportModuleDialog(
    private val project: Project,
    private val modules: List<Module>,
    private val groups: List<Group>,
    private val submitter: User
) : DialogWrapper(project) {
    private val selectedModule = AtomicProperty<Module>(modules.first())
    private val selectedGroup = AtomicProperty<Group>(Group.EXPORT_ALONE)
    private val outputPath = AtomicProperty(System.getProperty("user.home"))
    private val fileName = AtomicProperty("")

    init {
        title = message("ui.ExportModuleDialog.title")
        selectedGroup.whenPropertyChanged { updateFileName() }
        selectedModule.whenPropertyChanged { updateFileName() }
        init()
    }

    private fun updateFileName() {
        val group = selectedGroup.get()
        val module = selectedModule.get()
        val lastNames = if (group == Group.EXPORT_ALONE) submitter.userName.substringAfterLast(" ") else group.members
            .map { it.name.substringAfterLast(" ") }
            .sorted()
            .joinToString("_")
        val studentId = submitter.studentId?.let { "${submitter.studentId}_" } ?: ""
        fileName.set("${studentId}${lastNames}_${module.name}")
    }

    override fun createCenterPanel(): DialogPanel = panel {
        row(message("ui.ExportModuleDialog.selectModule")) {
            comboBox(modules, object : ListCellRenderer<Module?> {
                override fun getListCellRendererComponent(
                    list: JList<out Module?>,
                    value: Module?,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
                ): Component {
                    if (value == null) {
                        return JBLabel(message("ui.ExportModuleDialog.noModule"), JBLabel.LEFT)
                    }
                    return JBLabel(value.name, CoursesIcons.Module, JBLabel.LEFT)
                }
            })
                .bindItem(selectedModule)
                .applyToComponent {
                    selectedItem = null
                }
                .validationOnApply {
                    if (it.selectedItem == null) {
                        return@validationOnApply ValidationInfo(message("ui.ExportModuleDialog.error.noModule"))
                    }
                    return@validationOnApply null
                }
                .align(AlignX.FILL)
        }
        row(message("ui.ExportModuleDialog.selectGroup")) {
            comboBox(groups)
                .bindItem(selectedGroup)
                .align(AlignX.FILL)
        }
        row(message("ui.ExportModuleDialog.filename")) {
            textField()
                .bindText(fileName)
                .validationOnApply {
                    if (it.text.trim().isEmpty()) {
                        return@validationOnApply ValidationInfo(message("ui.ExportModuleDialog.error.noFilename"))
                    }
                    return@validationOnApply null
                }
                .align(AlignX.FILL)
        }
        row(message("ui.ExportModuleDialog.outputPath")) {
            textFieldWithBrowseButton(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle(message("ui.ExportModuleDialog.selectOutput")),
                project
            )
                .bindText(outputPath)
                .validationOnApply {
                    if (it.text.trim().isEmpty()) {
                        return@validationOnApply ValidationInfo(message("ui.ExportModuleDialog.error.noPath"))
                    }
                    return@validationOnApply null
                }
                .align(AlignX.FILL)
        }
    }

    fun getSelectedModule(): Module = selectedModule.get()
    fun getSelectedGroup(): Group = selectedGroup.get()
    fun getOutputPath(): Path? = outputPath.get().takeIf { it.isNotEmpty() }?.let { Path.of(it) }
    fun getFileName(): String = fileName.get()
} 