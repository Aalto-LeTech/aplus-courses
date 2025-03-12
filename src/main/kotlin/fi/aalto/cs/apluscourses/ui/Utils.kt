package fi.aalto.cs.apluscourses.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.lockOrSkip
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.AnActionLink
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import org.jetbrains.annotations.Nls
import java.awt.event.ActionEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.Icon
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel

object Utils {
    fun loadingPanel(): DialogPanel {
        return panel {
            row {
                icon(CoursesIcons.Loading).resizableColumn().align(Align.CENTER)
            }.resizableRow()
        }
    }

    fun Row.myLink(text: String, icon: Icon, action: (ActionEvent) -> Unit): Cell<ActionLink> =
        link(text, action).applyToComponent {
            setIcon(icon, false)
        }

    fun Row.myActionLink(text: String, icon: Icon, action: AnAction): Cell<AnActionLink> =
        cell(AnActionLink(text, action)).applyToComponent {
            setIcon(icon, false)
        }

    fun Row.directoryField(project: Project, @Nls title: String): Cell<TextFieldWithBrowseButton> {
        val field = TextFieldWithBrowseButton()
        field.addBrowseFolderListener(
            project, FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withTitle(title)
                .withHideIgnored(false)
        )

        return cell(field)
    }

    /**
     * A wrapper for JBList to be used with the UI DSL.
     */
    inline fun <reified T> Row.list(
        items: ObservableMutableProperty<List<T>>,
        renderer: ListCellRenderer<in T?>
    ): Cell<JBList<T>> {
        val list = JBList(items.get())
        list.cellRenderer = renderer
        list.putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        items.afterChange {
            list.setListData(it.toTypedArray())
        }
        return cell(list)
    }

    /**
     * Extension method to allow binding the selected item with a null value meaning no selection.
     */
    fun <T, C : JBList<T>> Cell<C>.bindItem(property: ObservableMutableProperty<T?>): Cell<C> {
        return applyToComponent {
            setSelectedValue(property.get(), false)
            val mutex = AtomicBoolean()
            property.afterChange {
                mutex.lockOrSkip {
                    setSelectedValue(it, false)
                }
            }
            this.addListSelectionListener {
                mutex.lockOrSkip {
                    property.set(selectedValue)
                }
            }
        }
    }

}