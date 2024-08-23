package fi.aalto.cs.apluscourses.utils

import com.intellij.ide.plugins.PluginEnabler
import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.ide.plugins.PluginManagerCore.isPluginInstalled
import com.intellij.ide.plugins.PluginNode
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.CourseConfig.RequiredPlugin
import fi.aalto.cs.apluscourses.services.Plugins
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PluginAutoInstaller {
    private fun shouldDownloadPlugin(id: PluginId): Boolean {
        return !isPluginInstalled(id)
    }

    private fun shouldEnablePlugin(id: PluginId): Boolean {
        val plugin = getPlugin(id)
        return plugin != null && !plugin.isEnabled
    }

    /**
     * Verifies that all plugins with given plugin IDs are installed and enabled. If not, the function
     * asks the user for consent to install and enable all missing plugins.
     *
     * @param project     The project which initiated the operation.
     * @param pluginNames List of plugin ID strings. The plugin ID can be found on the plugin's
     * JetBrains Marketplace website.
     * @param callback    An interface implementing functions invoked by this method. The functions
     * are related to showing the consent dialog box to the user.
     * @return True, if all dependencies are satisfied and there's nothing to be done. False, if
     * some dependencies were missing, and they were installed or enabled. Null, if the operation
     * has been cancelled due to missing consent or an error.
     */
    suspend fun ensureDependenciesInstalled(
        project: Project,
        pluginNames: List<RequiredPlugin>,
        askForConsent: Boolean = false
    ): Boolean? {
        val pluginIDs = pluginNames.map { PluginId.getId(it.id) }.toSet()
        // Downloading and enabling plugins are different operations, so we handle these separately.
        val pluginsToDownload = pluginIDs.filter { shouldDownloadPlugin(it) }.toSet()
        val pluginsToEnable = pluginIDs.filter { shouldEnablePlugin(it) }.toSet()

        pluginsToDownload.forEach { CoursesLogger.info("Plugin to download: ${it.idString}") }
        pluginsToEnable.forEach { CoursesLogger.info("Plugin to enable: ${it.idString}") }


        if (pluginsToDownload.isEmpty() && pluginsToEnable.isEmpty()) {
            return true
        }

        val missingPluginIds = (pluginsToDownload + pluginsToEnable).toList()
            .map { it.idString }
            .toSet()

        val missingPluginNames = pluginNames
            .filter { p: RequiredPlugin -> missingPluginIds.contains(p.id) }
            .map { p: RequiredPlugin -> p.name }


        if (askForConsent) {
            val install = withContext(Dispatchers.EDT) {
                Messages.showOkCancelDialog(
                    project,
                    message(
                        "ui.pluginInstallationDialog.courseOpenDialog.message",
                        missingPluginNames.joinToString(", ")
                    ),
                    message("ui.pluginInstallationDialog.courseOpenDialog.title"),
                    message("ui.pluginInstallationDialog.courseOpenDialog.yesText"),
                    message("ui.pluginInstallationDialog.courseOpenDialog.noText"),
                    Messages.getQuestionIcon()
                ) == Messages.OK
            }
            if (!install) {
                return true
            }
        }

        // Enable all requires plugins that are installed, but disabled.
        PluginEnabler.getInstance().enableById(pluginsToEnable)

        val downloadablePluginNodes = pluginsToDownload
            .map { PluginNode(it) }

        return application.service<Plugins>().installPlugins(project, downloadablePluginNodes)
    }
}
