package fi.aalto.cs.apluscourses.generator

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
import icons.PluginIcons
import javax.swing.Icon

//class APlusModuleType : ModuleType<APlusModuleBuilder>(ID) {
//    override fun createModuleBuilder(): APlusModuleBuilder {
//        return APlusModuleBuilder()
//    }
//
//    override fun getName(): String {
//        return PluginResourceBundle.getText("intellij.ProjectBuilder.name")
//    }
//
//    override fun getDescription(): String {
//        return PluginResourceBundle.getText("intellij.ProjectBuilder.description")
//    }
//
//    override fun getNodeIcon(isOpened: Boolean): Icon {
//        return PluginIcons.A_PLUS_LOGO_COLOR
//    }
//
//    companion object {
//        private const val ID = "APLUS_MODULE_TYPE"
//
//        val instance: APlusModuleType
//            get() = ModuleTypeManager.getInstance().findByID(ID) as APlusModuleType
//    }
//}
