package fi.aalto.cs.apluscourses.actions.module

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class InstallModuleAction : DumbAwareAction() {
    //    override fun update(e: AnActionEvent) {
//        val courseViewModel =
//            mainViewModelProvider.getMainViewModel(e.project).courseViewModel.get()
//        val isModuleSelected = courseViewModel != null
//                && !courseViewModel.modules.isSelectionEmpty
//        e.presentation.isEnabled = isModuleSelected
//    }
//
//    override fun getActionUpdateThread(): ActionUpdateThread {
//        return ActionUpdateThread.BGT
//    }
//
//    override fun actionPerformed(e: AnActionEvent) {
//        logger.debug("Starting InstallModuleAction")
//        val courseViewModel =
//            mainViewModelProvider.getMainViewModel(e.project).courseViewModel.get()
//        if (courseViewModel != null) {
//            val modules = courseViewModel.modules.streamSelectedElements()
//                .map { obj: ModuleListElementViewModel -> obj.model }
//                .collect(Collectors.toList())
//            logger.info("Downloading modules: %s".formatted(modules))
//            val course = courseViewModel.model
//            componentInstallerFactory.getInstallerFor(
//                course,
//                dialogsFactory.getDialogs(e.project), course.callbacks
//            )
//                .installAsync(modules) { downloadDone(course) }
//        }
//    }
//
//    private fun downloadDone(course: Course) {
//        course.validate()
//        logger.debug("Finished downloading modules")
//    }
//
//    companion object {
//        private val logger: Logger = APlusLogger.logger
//
//        val ACTION_ID: String = InstallModuleAction::class.java.canonicalName
//    }
    override fun actionPerformed(p0: AnActionEvent) {
        TODO("Not yet implemented")
    }
}
