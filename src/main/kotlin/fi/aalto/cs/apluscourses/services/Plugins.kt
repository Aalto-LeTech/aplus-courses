package fi.aalto.cs.apluscourses.services

import com.intellij.ide.plugins.PluginEnabler
import com.intellij.ide.plugins.PluginManagerMain
import com.intellij.ide.plugins.PluginNode
import com.intellij.ide.plugins.RepositoryHelper
import com.intellij.ide.plugins.enums.PluginsGroupType
import com.intellij.ide.plugins.newui.ListPluginComponent
import com.intellij.ide.plugins.newui.MyPluginModel
import com.intellij.ide.plugins.newui.PluginsGroup
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.util.application
import fi.aalto.cs.apluscourses.api.CourseConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.APP)
class Plugins(val cs: CoroutineScope) {
    fun runInBackground(
        requiredPlugins: List<CourseConfig.RequiredPlugin>,
        callback: (List<ListPluginComponent>) -> Unit
    ) {
        cs.launch {
            val nodes = RepositoryHelper.loadPlugins(requiredPlugins.map { PluginId.getId(it.id) }.toSet())
            val components = nodes.mapNotNull { pluginInfo ->
                ListPluginComponent(
                    MyPluginModel(null),
                    pluginInfo,
                    PluginsGroup("", PluginsGroupType.SEARCH),
                    object : LinkListener<Any> {
                        override fun linkSelected(aSource: LinkLabel<in Any>, aLinkData: Any) {}
                    },
                    false
                )
            }
            callback(components)
        }
    }

    suspend fun installPlugins(project: Project, downloadablePluginNodes: List<PluginNode>): Boolean =
        withBackgroundProgress(project, "A+ Courses") {
            reportSequentialProgress { reporter ->
                reporter.indeterminateStep("Installing plugins")
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