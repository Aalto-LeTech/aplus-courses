package fi.aalto.cs.apluscourses.utils

import com.intellij.ide.plugins.PluginManagerCore.isDisabled
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
import fi.aalto.cs.apluscourses.notifications.DisabledPluginsNotification
import fi.aalto.cs.apluscourses.services.course.CourseSetupStatus
import fi.aalto.cs.apluscourses.services.course.SetupAction
import fi.aalto.cs.apluscourses.services.course.SetupStepState
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.Plugins
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PluginAutoInstaller {
    internal fun shouldDownloadPlugin(id: PluginId): Boolean {
        return !isPluginInstalled(id)
    }

    internal fun shouldEnablePlugin(id: PluginId): Boolean {
        return isPluginInstalled(id) && isDisabled(id)
    }

    /**
     * Makes sure the plugins the course requires are installed and enabled.
     *
     * Missing plugins are downloaded, which takes effect after a restart. Plugins that are
     * installed but disabled are only reported, enabling one programmatically requires internal
     * platform API, so [DisabledPluginsNotification] points the user at the plugin manager instead.
     *
     * @param project     The project which initiated the operation.
     * @param pluginNames Plugins the course requires. The plugin ID can be found on the plugin's
     * JetBrains Marketplace website.
     * @return False when plugins were downloaded and the IDE has to restart for them to load.
     * True in all other cases.
     */
    suspend fun ensureDependenciesInstalled(
        project: Project,
        pluginNames: List<RequiredPlugin>,
        askForConsent: Boolean = false
    ): Boolean {
        val pluginIDs = pluginNames.map { PluginId.getId(it.id) }.toSet()
        // Downloading and enabling plugins are different operations, so we handle these separately.
        val pluginsToDownload = pluginIDs.filter { shouldDownloadPlugin(it) }.toSet()
        val disabledPlugins = pluginIDs.filter { shouldEnablePlugin(it) }.toSet()

        pluginsToDownload.forEach { CoursesLogger.info("Plugin to download: ${it.idString}") }
        disabledPlugins.forEach { CoursesLogger.info("Plugin is disabled: ${it.idString}") }

        registerRequiredPlugins(project, pluginNames)

        val setup = CourseSetupStatus.getInstance(project)
        if (disabledPlugins.isNotEmpty()) {
            val names = displayNamesOf(disabledPlugins, pluginNames)
            Notifier.notify(DisabledPluginsNotification(names, project), project)
            setup.report(
                CourseSetupStatus.PLUGINS,
                names.joinToString(", "),
                SetupStepState.ACTION_NEEDED,
                message("ui.setup.pluginsDisabled"),
                SetupAction.OPEN_PLUGIN_SETTINGS
            )
        }

        if (pluginsToDownload.isEmpty()) {
            if (disabledPlugins.isEmpty()) {
                setup.update(CourseSetupStatus.PLUGINS, SetupStepState.DONE)
            }
            return true
        }

        if (askForConsent) {
            val install = withContext(Dispatchers.EDT) {
                Messages.showOkCancelDialog(
                    project,
                    message(
                        "ui.pluginInstallationDialog.courseOpenDialog.message",
                        displayNamesOf(pluginsToDownload, pluginNames).joinToString(", ")
                    ),
                    message("ui.pluginInstallationDialog.courseOpenDialog.title"),
                    message("ui.pluginInstallationDialog.courseOpenDialog.yesText"),
                    message("ui.pluginInstallationDialog.courseOpenDialog.noText"),
                    Messages.getQuestionIcon()
                ) == Messages.OK
            }
            if (!install) {
                setup.update(
                    CourseSetupStatus.PLUGINS,
                    SetupStepState.ACTION_NEEDED,
                    message("ui.setup.pluginsDeclined"),
                    SetupAction.INSTALL_PLUGINS
                )
                return true
            }
        }

        val downloadablePluginNodes = pluginsToDownload.map { PluginNode(it) }

        val plugins = application.service<Plugins>()
        val result = plugins.installPlugins(project, downloadablePluginNodes)
        plugins.reportInstallResult(project, result)
        return !result.needsRestart
    }

    /**
     * Records the course's plugins in `.idea/externalDependencies.xml`.
     *
     * The platform's own `CheckProjectRequiredPluginsActivity` reads that file on project open.
     * It notifies the user with install and enable actions.
     */
    private suspend fun registerRequiredPlugins(project: Project, plugins: List<RequiredPlugin>) {
        val requiredIds = (listOf(PluginSettings.PLUGIN_ID) + plugins.map { it.id }).distinct()
        RequiredPluginsFile.write(project, requiredIds)
    }

    private fun displayNamesOf(ids: Set<PluginId>, pluginNames: List<RequiredPlugin>): List<String> {
        val idStrings = ids.map { it.idString }.toSet()
        return pluginNames.filter { it.id in idStrings }.map { it.name }
    }
}
