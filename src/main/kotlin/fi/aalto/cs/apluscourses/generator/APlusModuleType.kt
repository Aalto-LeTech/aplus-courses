package fi.aalto.cs.apluscourses.generator

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

internal class APlusModuleType : ModuleType<APlusModuleBuilder>(ID) {
    override fun createModuleBuilder(): APlusModuleBuilder {
        return APlusModuleBuilder()
    }

    override fun getName(): String {
        return MyBundle.message("intellij.ProjectBuilder.name")
    }

    override fun getDescription(): String {
        return MyBundle.message("intellij.ProjectBuilder.description")
    }

    override fun getNodeIcon(isOpened: Boolean): Icon {
        return CoursesIcons.LogoColor
    }

    companion object {
        @NonNls
        const val ID = "APLUS_MODULE_TYPE"

        val instance: APlusModuleType
            get() = ModuleTypeManager.getInstance().findByID(ID) as APlusModuleType
    }
}
