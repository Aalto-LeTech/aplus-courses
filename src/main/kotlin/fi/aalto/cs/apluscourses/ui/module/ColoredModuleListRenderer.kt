package fi.aalto.cs.apluscourses.ui.module

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.speedSearch.SpeedSearchUtil
import fi.aalto.cs.apluscourses.presentation.module.ModuleListElementViewModel
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
import icons.PluginIcons
import javax.swing.JList

class ColoredModuleListRenderer

    : ColoredListCellRenderer<ModuleListElementViewModel>() {
    override fun customizeCellRenderer(
        list: JList<out ModuleListElementViewModel>,
        element: ModuleListElementViewModel,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        append(
            element.name,
            if (element.isBoldface) BOLDED_TEXT_STYLE else SimpleTextAttributes.REGULAR_ATTRIBUTES, true
        )
        append("  [" + element.status + "]", STATUS_TEXT_STYLE)
        toolTipText = element.tooltip
        icon = PluginIcons.A_PLUS_MODULE
        iconTextGap = 4
        if (element.isUpdateAvailable && (!element.model.hasLocalChanges()
                    || element.model.isMajorUpdate)
        ) {
            append(
                "  " + PluginResourceBundle.getText("ui.toolWindow.subTab.modules.module.updateAvailable"),
                BOLDED_TEXT_STYLE
            )
        }

        SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected)
    }

    companion object {
        private val BOLDED_TEXT_STYLE = SimpleTextAttributes(
            SimpleTextAttributes.STYLE_BOLD, null
        )
        private val STATUS_TEXT_STYLE = SimpleTextAttributes(
            SimpleTextAttributes.STYLE_ITALIC or SimpleTextAttributes.STYLE_SMALLER, null
        )
    }
}
