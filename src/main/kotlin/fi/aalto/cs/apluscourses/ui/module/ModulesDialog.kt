package fi.aalto.cs.apluscourses.ui.module

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import fi.aalto.cs.apluscourses.toolwindows.createModulesView
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
import javax.swing.Action
import javax.swing.JComponent

class ModulesDialog(project: Project) : DialogWrapper(project) {
    private val modulesView = createModulesView(project)

    /**
     * Constructor.
     */
    init {
        isModal = false

        init()
        setSize(400, 600)
    }

    override fun createActions(): Array<Action> {
        return arrayOf(DialogWrapperExitAction(PluginResourceBundle.getText("ui.close"), OK_EXIT_CODE))
    }

    override fun getPreferredFocusedComponent(): JComponent {
        TODO()//return modulesView.
    }

    override fun createCenterPanel(): JComponent? {
        return modulesView.content
    }
}
