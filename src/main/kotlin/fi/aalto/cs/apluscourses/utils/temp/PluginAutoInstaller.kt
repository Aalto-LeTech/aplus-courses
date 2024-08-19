package fi.aalto.cs.apluscourses.utils.temp

import com.intellij.ide.plugins.PluginEnabler
import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.ide.plugins.PluginManagerCore.isPluginInstalled
import com.intellij.ide.plugins.PluginNode
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.util.application
import fi.aalto.cs.apluscourses.api.CourseConfig.RequiredPlugin
import fi.aalto.cs.apluscourses.services.Plugins
import fi.aalto.cs.apluscourses.utils.APlusLogger

object PluginAutoInstaller {
    private val logger: com.intellij.openapi.diagnostic.Logger = APlusLogger.logger

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
//        callback: PluginInstallerCallback
    ): Boolean? {
        val pluginIDs = pluginNames.map { PluginId.getId(it.id) }.toSet()
        // Downloading and enabling plugins are different operations, so we handle these separately.
        val pluginsToDownload = pluginIDs.filter { shouldDownloadPlugin(it) }.toSet()
        val pluginsToEnable = pluginIDs.filter { shouldEnablePlugin(it) }.toSet()

        pluginsToDownload.forEach { logger.info("Plugin to download: ${it.idString}") }
        pluginsToEnable.forEach { logger.info("Plugin to enable: ${it.idString}") }


        if (pluginsToDownload.isEmpty() && pluginsToEnable.isEmpty()) {
            return true
        }

        // allPluginNames = list of PluginDependencies for all plugins in pluginsToDownload or pluginsToEnable
        val missingPluginIds = (pluginsToDownload + pluginsToEnable).toList()
            .map { it.idString }
            .toSet()

        pluginNames
            .filter { p: RequiredPlugin -> missingPluginIds.contains(p.id) }


//        val consentResult: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
//            callback.askForInstallationConsent(allPluginNames)
//        if (consentResult == PluginInstallerCallback.ConsentResult.IGNORE_INSTALL) {
//            return true // pretend that everything went fine
//        } else if (consentResult == PluginInstallerCallback.ConsentResult.REJECTED) {
//            return null // signal to the caller to abort the operation
//        }

        // Enable all requires plugins that are installed, but disabled.
        PluginEnabler.getInstance().enableById(pluginsToEnable)

        val downloadablePluginNodes = pluginsToDownload
            .map { PluginNode(it) }

        return application.service<Plugins>().installPlugins(project, downloadablePluginNodes)
    }
}
