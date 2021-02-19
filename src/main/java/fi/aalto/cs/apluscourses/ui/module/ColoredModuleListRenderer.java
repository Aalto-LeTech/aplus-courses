package fi.aalto.cs.apluscourses.ui.module;

import static com.intellij.ui.SimpleTextAttributes.REGULAR_ATTRIBUTES;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import fi.aalto.cs.apluscourses.presentation.module.ModuleListElementViewModel;
import icons.PluginIcons;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;

public class ColoredModuleListRenderer
        extends ColoredListCellRenderer<ModuleListElementViewModel> {

  private static final SimpleTextAttributes BOLDED_TEXT_STYLE = new SimpleTextAttributes(
          SimpleTextAttributes.STYLE_BOLD, null);
  private static final SimpleTextAttributes STATUS_TEXT_STYLE = new SimpleTextAttributes(
          SimpleTextAttributes.STYLE_ITALIC | SimpleTextAttributes.STYLE_SMALLER, null);

  @Override
  protected void customizeCellRenderer(@NotNull JList<? extends ModuleListElementViewModel> list,
                                       ModuleListElementViewModel element,
                                       int index,
                                       boolean selected,
                                       boolean hasFocus) {
    append(element.getName(),
            element.isBoldface() ? BOLDED_TEXT_STYLE : REGULAR_ATTRIBUTES, true);
    append("  [" + element.getStatus() + "]", STATUS_TEXT_STYLE);
    setToolTipText(element.getTooltip());
    setIcon(PluginIcons.A_PLUS_MODULE);
    setIconTextGap(4);
    if (element.isUpdateAvailable()) {
      append("  " + getText("ui.toolWindow.subTab.modules.module.updateAvailable"),
          BOLDED_TEXT_STYLE);
    }

    SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
  }
}
