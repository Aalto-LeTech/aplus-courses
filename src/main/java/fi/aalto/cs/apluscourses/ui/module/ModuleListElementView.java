package fi.aalto.cs.apluscourses.ui.module;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import fi.aalto.cs.apluscourses.presentation.module.ModuleListElementViewModel;
import fi.aalto.cs.apluscourses.ui.base.ListElementView;
import icons.PluginIcons;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JList;

import org.jetbrains.annotations.NotNull;

public class ModuleListElementView implements ListElementView {
  private static class ColoredModuleListRenderer
          extends ColoredListCellRenderer<ModuleListElementViewModel> {

    private static final SimpleTextAttributes STATUS_TEXT_STYLE = new SimpleTextAttributes(
            SimpleTextAttributes.STYLE_ITALIC | SimpleTextAttributes.STYLE_SMALLER, null);
    private static final SimpleTextAttributes UPDATE_TEXT_STYLE = new SimpleTextAttributes(
            SimpleTextAttributes.STYLE_BOLD, null);

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends ModuleListElementViewModel> list,
                                         ModuleListElementViewModel element,
                                         int index,
                                         boolean selected,
                                         boolean hasFocus) {
      append(element.getName(), element.getTextAttribute(), true);
      append("  [" + element.getStatus() + "]", STATUS_TEXT_STYLE);
      setToolTipText(element.getTooltip());
      setIcon(PluginIcons.A_PLUS_MODULE);
      setIconTextGap(4);
      if (!element.isUpdateAvailable()) {
        append("  UPDATE AVAILABLE!", UPDATE_TEXT_STYLE);
      }

      SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
    }
  }

  public static final ColoredModuleListRenderer CELL_RENDERER = new ColoredModuleListRenderer();

  @Override
  public JComponent getRenderer() {
    return CELL_RENDERER;
  }
}
