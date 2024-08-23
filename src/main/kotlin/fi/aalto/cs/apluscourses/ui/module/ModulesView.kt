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
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.ui.Utils.loadingPanel
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class ModulesView(val project: Project) : SimpleToolWindowPanel(true, true) {
    private var modules = mutableListOf<Module>()
    private var actionRequired = mutableListOf<Module>()
    private var available = mutableListOf<Module>()
    private var installed = mutableListOf<Module>()
    private val itemPanels = mutableListOf<ModuleRenderer>()
    val searchTextField: SearchTextField = object : SearchTextField(false) {
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
            emptyText.text = "Search Modules.."
            accessibleContext.accessibleName = "Search Modules"
            TextComponentEmptyText.setupPlaceholderVisibility(this)
        }
    }

    init {
        setContent(loadingPanel())
    }

    private fun collapseAll() = itemPanels.forEach { it.collapse() }

    fun searchChanged(text: String) {
        for (item in itemPanels) {
            item.setVisibility(item.module.name.lowercase().contains(text.lowercase()))
        }
    }

    private fun getPanelAt(point: Point) =
        itemPanels.find { it.bounds.contains(point) && it.isVisible }

    private fun updateView() {
        val openModule = itemPanels.find { it.isExpanded }
        val categories = modules.groupBy { it.category }
        actionRequired = categories[Module.Category.ACTION_REQUIRED]?.toMutableList() ?: mutableListOf()
        available = categories[Module.Category.AVAILABLE]?.toMutableList() ?: mutableListOf()
        installed = categories[Module.Category.INSTALLED]?.toMutableList() ?: mutableListOf()

        itemPanels.clear()

        fun Panel.addCategory(category: Module.Category, modules: List<Module>) {
            val categoryName = when (category) {
                Module.Category.ACTION_REQUIRED -> "Action Required"
                Module.Category.AVAILABLE -> "Available Modules"
                Module.Category.INSTALLED -> "Installed Modules"
            }
            if (modules.isNotEmpty()) {
                group("  $categoryName", indent = false) {
                    modules.forEachIndexed { index, module ->
                        val itemPanel = ModuleRenderer(module, index, project)
                        itemPanels.add(itemPanel)
                        row {
                            cell(itemPanel).resizableColumn().align(AlignX.FILL)
                        }
                    }
                }
            }
        }

        val scrollValue = if (content is JBScrollPane) {
            (content as JBScrollPane?)?.verticalScrollBar?.value ?: 0
        } else 0

        val panel = panel {
            addCategory(Module.Category.ACTION_REQUIRED, actionRequired)
            addCategory(Module.Category.AVAILABLE, available)
            addCategory(Module.Category.INSTALLED, installed)
        }

        val content = JBScrollPane(panel)
        content.verticalScrollBar.value = scrollValue
        setContent(content)

        panel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val hoveringPanel = getPanelAt(e.point) ?: return
                itemPanels.forEach { it.updateBackground(false) }
                hoveringPanel.updateBackground(true)
            }
        })
        panel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                getPanelAt(e.point)?.let {
                    val isExpanded = it.isExpanded
                    collapseAll()
                    if (!isExpanded) {
                        it.expand()
                    }
                }
            }

            override fun mouseExited(e: MouseEvent) {
                val bounds = content.bounds
                val point = e.point
                point.translate(0, -content.verticalScrollBar.value)
                val scrollbarWidth = content.verticalScrollBar.width
                if (!bounds.contains(point) || point.x > bounds.width - scrollbarWidth) {
                    itemPanels.forEach { it.updateBackground(false) }
                }
            }
        })

        searchChanged(searchTextField.text)
        openModule?.let { showModule(it.module, false) }
        itemPanels.find { it.module.name == openModule?.module?.name }?.expand()
    }

    fun showModule(module: Module, scroll: Boolean) {
        collapseAll()
        val item = itemPanels.find { it.module.name == module.name }
        if (item != null) {
            searchTextField.text = ""
            searchChanged("")
            item.expand()
            if (scroll) (content as JBScrollPane?)?.verticalScrollBar?.value = item.location.y - 100
        }
    }

    fun viewModelChanged(course: Course?) {
        application.invokeLater {
            if (course == null) {
                itemPanels.clear()
                actionRequired.clear()
                installed.clear()
                available.clear()
                return@invokeLater
            }
            val visible = course.modules
            modules = visible.toMutableList()
            updateView()
        }
    }
}
