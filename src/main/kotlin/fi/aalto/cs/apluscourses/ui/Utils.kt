package fi.aalto.cs.apluscourses.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.lockOrSkip
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.AnActionLink
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.event.ActionEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JViewport
import javax.swing.ScrollPaneConstants
import javax.swing.Scrollable
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

    /**
     * Wraps [content] in a vertical scroll pane that gives it the viewport height while it fits.
     */
    fun stretchingScrollPane(content: JComponent): JBScrollPane {
        val view = object : JPanel(BorderLayout()), Scrollable {
            override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

            override fun getScrollableUnitIncrement(
                visibleRect: Rectangle,
                orientation: Int,
                direction: Int
            ): Int = JBUI.scale(SCROLL_UNIT)

            override fun getScrollableBlockIncrement(
                visibleRect: Rectangle,
                orientation: Int,
                direction: Int
            ): Int = visibleRect.height

            override fun getScrollableTracksViewportWidth(): Boolean = true

            override fun getScrollableTracksViewportHeight(): Boolean {
                val viewport = parent as? JViewport ?: return true
                return viewport.height >= preferredSize.height
            }
        }
        view.add(content, BorderLayout.CENTER)

        return JBScrollPane(view).apply {
            border = JBUI.Borders.empty()
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
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
     * A JBList kept in sync with an observable list of items.
     *
     * This returns the list itself, not a UI DSL cell. The caller must wrap it in a scroll pane,
     * so that the height comes from the layout and not from the number of items.
     */
    fun <T> list(
        items: ObservableMutableProperty<List<T>>,
        renderer: ListCellRenderer<in T?>
    ): JBList<T> {
        val model = CollectionListModel(items.get())
        // JList returns false for a vertical layout, which lets the widest item set the
        // viewport width.
        val list = object : JBList<T>(model) {
            override fun getScrollableTracksViewportWidth(): Boolean = true
        }
        list.cellRenderer = renderer
        list.putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        items.afterChange {
            model.replaceAll(it)
        }
        return list
    }

    /**
     * Allows binding the selected item with a null value meaning no selection.
     */
    fun <T> JBList<T>.bindItem(property: ObservableMutableProperty<T?>): JBList<T> {
        setSelectedValue(property.get(), false)
        val mutex = AtomicBoolean()
        property.afterChange {
            mutex.lockOrSkip {
                setSelectedValue(it, false)
            }
        }
        addListSelectionListener {
            mutex.lockOrSkip {
                property.set(selectedValue)
            }
        }
        return this
    }


    private const val SCROLL_UNIT = 16
}
