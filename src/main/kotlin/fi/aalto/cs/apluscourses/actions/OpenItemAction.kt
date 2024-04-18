package fi.aalto.cs.apluscourses.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

open class OpenItemAction<T> : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    /**
     * Construct an [OpenItemAction] instance with the given parameters. This constructor
     * is mainly useful for testing purposes.
     */

    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }
//        val treeViewModel: BaseTreeViewModel<T> = getTreeViewModel(e.getProject()) ?: return
//
//        val nodeViewModel: SelectableNodeViewModel<*> = treeViewModel.getSelectedItem() ?: return
//
//        try {
//            urlRenderer.show((nodeViewModel.getModel() as Browsable).getHtmlUrl())
//        } catch (ex: Exception) {
//            notifier.notify(UrlRenderingErrorNotification(ex), e.getProject())
//        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.setEnabled(
            e.project != null
//                    && getTreeViewModel(e.project) != null
        )
    }

//    val actionUpdateThread: ActionUpdateThread
//        get() = ActionUpdateThread.BGT
//
//    fun getTreeViewModel(project: Project): BaseTreeViewModel<T>? {
//        return null
//    }
}
