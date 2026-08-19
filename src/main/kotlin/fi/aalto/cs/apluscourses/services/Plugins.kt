package fi.aalto.cs.apluscourses.services

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.PluginNode
import com.intellij.ide.plugins.RepositoryHelper
import com.intellij.ide.plugins.newui.PluginLogo
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.updateSettings.impl.PluginDownloader
import com.intellij.platform.ide.progress.withBackgroundProgress
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.services.course.CourseSetupStatus
import fi.aalto.cs.apluscourses.services.course.SetupAction
import fi.aalto.cs.apluscourses.services.course.SetupStepState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.swing.Icon

@Service(Service.Level.APP)
class Plugins(val cs: CoroutineScope) {
    data class PluginInfo(
        val name: String,
        val version: String,
        val vendor: String?,
        val description: String?,
        val icon: Icon
    )

    fun runInBackground(
        requiredPlugins: List<CourseConfig.RequiredPlugin>,
        callback: (List<PluginInfo>) -> Unit
    ) {
        cs.launch {
            val nodes = RepositoryHelper.loadPlugins(requiredPlugins.map { PluginId.getId(it.id) }.toSet())
            val components = nodes.mapNotNull { pluginInfo ->
                val icon = PluginLogo.getIcon(pluginInfo, big = true, error = false, disabled = false)

                PluginInfo(
                    pluginInfo.name,
                    pluginInfo.version,
                    pluginInfo.vendor,
                    pluginInfo.description,
                    icon
                )
            }
            callback(components)
        }
    }

    fun installMissing(project: Project, ids: Set<PluginId>) {
        val missing = ids.filter { !PluginManagerCore.isPluginInstalled(it) }
        if (missing.isEmpty()) return

        val setup = CourseSetupStatus.getInstance(project)
        setup.update(CourseSetupStatus.PLUGINS, SetupStepState.RUNNING)
        cs.launch {
            reportInstallResult(project, installPlugins(project, missing.map { PluginNode(it) }))
        }
    }

    fun reportInstallResult(project: Project, result: InstallResult) {
        val setup = CourseSetupStatus.getInstance(project)
        when {
            result.failed == 0 -> setup.update(
                CourseSetupStatus.PLUGINS,
                SetupStepState.ACTION_NEEDED,
                message("ui.setup.pluginsRestart"),
                SetupAction.RESTART_IDE
            )

            // Some plugins are staged, so a restart is still necessary.
            result.needsRestart -> setup.update(
                CourseSetupStatus.PLUGINS,
                SetupStepState.ACTION_NEEDED,
                message("ui.setup.pluginsPartial"),
                SetupAction.RESTART_IDE
            )

            else -> setup.update(
                CourseSetupStatus.PLUGINS,
                SetupStepState.FAILED,
                message("ui.setup.pluginsFailed")
            )
        }
    }

    data class InstallResult(val staged: Int, val failed: Int) {
        val needsRestart: Boolean get() = staged > 0
    }

    suspend fun installPlugins(project: Project, downloadablePluginNodes: List<PluginNode>): InstallResult =
        withBackgroundProgress(project, message("services.progress.pluginsInstall")) {
            val installed = coroutineToIndicator { indicator ->
                downloadablePluginNodes.map { node ->
                    val downloader = PluginDownloader.createDownloader(node)
                    if (downloader.prepareToInstall(indicator)) {
                        downloader.install()
                        true
                    } else {
                        false
                    }
                }
            }

            InstallResult(staged = installed.count { it }, failed = installed.count { !it })
        }
}
