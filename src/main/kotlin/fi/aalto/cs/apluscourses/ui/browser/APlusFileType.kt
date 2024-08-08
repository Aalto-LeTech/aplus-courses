package fi.aalto.cs.apluscourses.ui.browser

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import icons.PluginIcons
import javax.swing.Icon

internal class APlusFileType : LanguageFileType(APlusLanguage.INSTANCE) {
    override fun getName(): String = "A+"

    override fun getDescription(): String = "Browse A+ courses inside the IDE"

    override fun getDefaultExtension(): String = "aplus"

    override fun getIcon(): Icon = PluginIcons.A_PLUS_LOGO_COLOR

    companion object {
        val INSTANCE = APlusFileType()
    }
}

private class APlusLanguage : Language("A+") {
    companion object {
        val INSTANCE = APlusLanguage()
    }
}