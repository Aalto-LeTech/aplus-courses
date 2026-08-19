package fi.aalto.cs.apluscourses.ui.module

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.TextComponentEmptyText
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.toolwindows.SearchableTab
import fi.aalto.cs.apluscourses.ui.Utils.loadingPanel
import java.awt.Point
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities

class ModulesView(val project: Project) : SimpleToolWindowPanel(true, true), SearchableTab {
    private val itemPanels = mutableListOf<ModuleRenderer>()
    private val scrollPane = JBScrollPane()
    override val searchTextField: SearchTextField = object : SearchTextField(false) {
        override fun preprocessEventForTextField(e: KeyEvent): Boolean {
            super.preprocessEventForTextField(e)
            searchChanged(this.text)
            return false
        }

        override fun onFieldCleared() {
            searchChanged("")
        }
    }.apply {
        textEditor.apply {
            emptyText.text = message("ui.ModuleView.search")
            accessibleContext.accessibleName = message("ui.ModuleView.searchAccessible")
            TextComponentEmptyText.setupPlaceholderVisibility(this)
        }
    }

    init {
        setContent(loadingPanel())
    }

    private fun collapseAll() = itemPanels.forEach { it.collapse() }

    fun searchChanged(text: String) {
        val query = text.lowercase()
        var visibleIndex = 0
        for (item in itemPanels) {
            item.isVisible = item.module.name.lowercase().contains(query)
            if (item.isVisible) {
                // The index decides the row's striping color, so it counts visible rows only.
                item.index = visibleIndex++
                item.refreshBackground()
            }
        }
    }

    private fun toggleExpanded(row: ModuleRenderer) {
        val wasExpanded = row.isExpanded
        collapseAll()
        if (!wasExpanded) {
            row.expand()
        }
    }

    private fun updateView(modules: List<Module>) {
        val openModule = itemPanels.find { it.isExpanded }
        val categories = modules.groupBy { it.category }

        itemPanels.clear()

        fun Panel.addCategory(category: Module.Category) {
            val categoryModules = categories[category] ?: return
            val categoryName = when (category) {
                Module.Category.ACTION_REQUIRED -> message("ui.ModuleView.category.actionRequired")
                Module.Category.AVAILABLE -> message("ui.ModuleView.category.available")
                Module.Category.INSTALLED -> message("ui.ModuleView.category.installed")
            }
            group("  $categoryName", indent = false) {
                categoryModules.forEachIndexed { index, module ->
                    val itemPanel = ModuleRenderer(module, index, project, ::toggleExpanded)
                    itemPanels.add(itemPanel)
                    row {
                        cell(itemPanel).resizableColumn().align(AlignX.FILL)
                    }
                }
            }
        }

        val scrolledTo = scrollPane.viewport.viewPosition.y

        val panel = panel {
            addCategory(Module.Category.ACTION_REQUIRED)
            addCategory(Module.Category.AVAILABLE)
            addCategory(Module.Category.INSTALLED)
        }

        scrollPane.setViewportView(panel)
        scrollPane.viewport.viewPosition = Point(0, scrolledTo)
        if (content !== scrollPane) {
            setContent(scrollPane)
        }

        searchChanged(searchTextField.text)
        openModule?.let { showModule(it.module, false) }
    }

    fun updateModuleIcon(module: Module) {
        itemPanels.find { it.module.name == module.name }?.refreshIcon()
    }

    fun showModule(module: Module, scroll: Boolean) {
        collapseAll()
        val item = itemPanels.find { it.module.name == module.name } ?: return
        item.expand()
        if (!scroll) return

        searchTextField.text = ""
        searchChanged("")
        val view = scrollPane.viewport.view ?: return
        val y = SwingUtilities.convertPoint(item, 0, 0, view).y
        scrollPane.verticalScrollBar.value = y - SCROLL_MARGIN
    }

    fun viewModelChanged(course: Course?) {
        application.invokeLater({
            if (course == null) {
                itemPanels.clear()
            } else {
                updateView(course.modules)
            }
        }, project.disposed)
    }

    private companion object {
        /** How much of the list is left visible above a row that has been scrolled to. */
        const val SCROLL_MARGIN = 100
    }
}
