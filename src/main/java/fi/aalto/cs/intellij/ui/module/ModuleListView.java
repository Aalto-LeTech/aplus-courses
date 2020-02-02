package fi.aalto.cs.intellij.ui.module;

import fi.aalto.cs.intellij.presentation.ModuleListElementModel;
import fi.aalto.cs.intellij.ui.common.ComponentUtil;
import fi.aalto.cs.intellij.ui.list.BaseListView;
import java.awt.font.TextAttribute;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ModuleListView extends BaseListView<ModuleListElementModel, ModuleListElementView> {

  @NotNull
  @Override
  protected ModuleListElementView createElementView(ModuleListElementModel element) {
    return new ModuleListElementView();
  }

  @Override
  protected void updateElementView(ModuleListElementView view, ModuleListElementModel element) {
    view.nameLabel.setText(element.getName());
    ComponentUtil.setFont(view.nameLabel, TextAttribute.WEIGHT, element.getFontWeight());
    view.statusLabel.setText("[" + element.getStatus() + "]");
    view.basePanel.setToolTipText(element.getUrl());
  }

  @NotNull
  @Override
  protected JComponent renderElementView(ModuleListElementView view) {
    return view.basePanel;
  }
}
