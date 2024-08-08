package fi.aalto.cs.apluscourses.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import fi.aalto.cs.apluscourses.utils.APlusLogger

class ResetSettingsAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
//    PluginSettings.getInstance().resetLocalSettings();
        logger.info("Reset local settings")
    }

    companion object {
        private val logger = APlusLogger.logger
    }
}
