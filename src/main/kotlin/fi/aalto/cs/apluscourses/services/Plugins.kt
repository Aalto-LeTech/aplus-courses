package fi.aalto.cs.apluscourses.services

import com.intellij.ide.plugins.PluginEnabler
import com.intellij.ide.plugins.PluginManagerMain
import com.intellij.ide.plugins.PluginNode
import com.intellij.ide.plugins.RepositoryHelper
import com.intellij.ide.plugins.newui.PluginLogo
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.CourseConfig
import kotlinx.coroutines.CompletableDeferred
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

    suspend fun installPlugins(project: Project, downloadablePluginNodes: List<PluginNode>): Boolean =
        withBackgroundProgress(project, message("aplusCourses")) {
            reportSequentialProgress { reporter ->
                reporter.indeterminateStep(message("services.progress.pluginsInstall"))
                val deferredResult = CompletableDeferred<Boolean>()
                application.runWriteAction {
                    PluginManagerMain.downloadPlugins(
                        downloadablePluginNodes,
                        emptyList(),
                        false,
                        null,
                        PluginEnabler.getInstance(),
                        ModalityState.defaultModalityState()
                    ) { success -> deferredResult.complete(success) }
                }

                !deferredResult.await()
            }
        }
}