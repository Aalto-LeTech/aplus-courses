package fi.aalto.cs.apluscourses.ui.module

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import fi.aalto.cs.apluscourses.presentation.CourseViewModel
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle

class ModulesView : SimpleToolWindowPanel(true, true) {
    @JvmField
    val moduleListView: ModuleListView = ModuleListView()
//
//    @GuiObject
//    var toolbarContainer: JPanel? = null
//
//    @GuiObject
//    private val basePanel: JPanel? = null
//
//    @GuiObject
//    private var pane: JScrollPane? = null

    /**
     * A view that holds the content of the Modules tool window.
     */
    init {
        // Avoid this instance getting GC'd before its UI components.
        //
        // Here we add a (strong) reference from a UI component to this object, thus ensuring that this
        // object lives at least as long as that UI component.
        //
        // This makes it possible to use this object as a weakly referred observer for changes that
        // require UI updates.
        //
        // If UI components are GC'd, this object can also go.
        //
        // It depends on the implementation of IntelliJ's GUI designer whether this "hack"
        // needed (I don't know if these objects of bound classes are strongly referred to from UI or
        // not), but it's better to play it safe.
        //
        // We use class name as a unique key for the property.
        val content = JBScrollPane(moduleListView)
        setContent(content)
        content.putClientProperty(ModulesView::class.java.name, this)

        moduleListView.background = Tree().background
        content.verticalScrollBar.unitIncrement = moduleListView.fixedCellHeight
        moduleListView.setEmptyText(PluginResourceBundle.getText("ui.toolWindow.loading"))
    }

    /**
     * Update this modules view with the given view model (which may be null).
     */
    fun viewModelChanged(course: CourseViewModel?) {
        ApplicationManager.getApplication().invokeLater(
            {
                moduleListView.setViewModel(course?.modules)
                moduleListView.setEmptyText(
                    if (course == null) PluginResourceBundle.getText("ui.toolWindow.loading") else PluginResourceBundle.getText(
                        "ui.toolWindow.subTab.modules.noModules"
                    )
                )
            }, ModalityState.any()
        )
    }
}
