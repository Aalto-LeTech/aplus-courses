package fi.aalto.cs.apluscourses.services

import com.intellij.ide.plugins.RepositoryHelper
import com.intellij.ide.plugins.enums.PluginsGroupType
import com.intellij.ide.plugins.newui.ListPluginComponent
import com.intellij.ide.plugins.newui.MyPluginModel
import com.intellij.ide.plugins.newui.PluginsGroup
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.api.CourseConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.APP)
class Plugins(val cs: CoroutineScope) {
    fun runInBackground(requiredPlugins: List<CourseConfig.RequiredPlugin>, updateUI: (DialogPanel) -> Unit) {
        cs.launch {
            val nodes = RepositoryHelper.loadPlugins(requiredPlugins.map { PluginId.getId(it.id) }.toSet())
            val components = nodes.mapNotNull { pluginInfo ->
//                println("pluginInfo: $pluginInfo")
////                val testestset = SearchQueryParser.Marketplace(pluginInfo.id)
//                val node = PluginNode(PluginId.getId(pluginInfo.id))
//                node.name = pluginInfo.name
////                val plugin = PluginManagerCore.findPlugin(PluginId.getId(pluginInfo.id)) ?: return@forEach
//                println("plugin: $node")
//                val hosts = RepositoryHelper.getPluginHosts()
//                val allPlugins: MutableMap<PluginId, PluginNode> = mutableMapOf()
//                RepositoryHelper.loadPlugins(setOf( PluginId.getId(pluginInfo.id)))
//                hosts.forEach { host ->
//                    println("host: $host")
//                    RepositoryHelper.loadPlugins(host, null, null).forEach { descriptor ->
////                        println("descriptor: $descriptor")
//                        allPlugins[descriptor.pluginId] = descriptor
//                    }
//                }
//                println("allPlugins: ${allPlugins.size}")
////                val icon = PluginLogo.getIcon(plugin, false, false, false)
////                test2.icon = icon
//                val plugin = allPlugins[PluginId.getId(pluginInfo.id)]
//                println("plugin: $plugin")
//                if (plugin == null) return@mapNotNull null

                val component = ListPluginComponent(
                    MyPluginModel(null),
                    pluginInfo,
                    PluginsGroup("", PluginsGroupType.SEARCH),
                    object : LinkListener<Any> {
                        override fun linkSelected(
                            aSource: LinkLabel<in Any>?,
                            aLinkData: Any?
                        ) {
                            println("linkSelected")
                        }
                    },
                    false
                )
                component.remove(4)
                component.remove(3)
                component

            }
            updateUI(
                panel {
                    components.forEach {
                        row {
                            cell(it)
                        }.topGap(TopGap.SMALL)
                    }
                }
            )
        }
    }
}