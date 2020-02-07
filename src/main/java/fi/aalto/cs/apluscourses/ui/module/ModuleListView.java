package fi.aalto.cs.apluscourses.ui.module;

import fi.aalto.cs.apluscourses.presentation.module.ModuleListElementViewModel;
import fi.aalto.cs.apluscourses.ui.base.BaseListView;
import fi.aalto.cs.apluscourses.ui.base.ComponentUtil;
import java.awt.font.TextAttribute;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleListView
    extends BaseListView<ModuleListElementViewModel, ModuleListElementView> {

  @NotNull
  @Override
  protected ModuleListElementView createElementView(@Nullable ModuleListElementViewModel element) {
    return new ModuleListElementView();
  }

  @Override
  protected void updateElementView(@NotNull ModuleListElementView view,
                                   @NotNull ModuleListElementViewModel element) {
    view.nameLabel.setText(element.getName());
    ComponentUtil.setFont(view.nameLabel, TextAttribute.WEIGHT, element.getFontWeight());
    view.statusLabel.setText("[" + element.getStatus() + "]");
    view.basePanel.setToolTipText(element.getUrl());
  }

  @NotNull
  @Override
  protected JComponent renderElementView(@NotNull ModuleListElementView view) {
    return view.basePanel;
  }
}
