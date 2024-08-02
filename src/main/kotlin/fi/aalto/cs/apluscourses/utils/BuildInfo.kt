package fi.aalto.cs.apluscourses.utils

import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.openapi.extensions.PluginId

object BuildInfo {
    val pluginVersion: String = getPlugin(PluginId.getId("fi.aalto.cs.intellij-plugin"))?.version ?: ""
}
