package fi.aalto.cs.apluscourses.ui.module;

import com.intellij.ui.ListSpeedSearch;
import fi.aalto.cs.apluscourses.presentation.module.ModuleListElementViewModel;
import fi.aalto.cs.apluscourses.ui.base.BaseListView;

import org.jetbrains.annotations.NotNull;

public class ModuleListView
        extends BaseListView<ModuleListElementViewModel, ModuleListElementView> {
  /**
   * Constructs a view for the module list.
   */
  public ModuleListView() {
    super(new ModuleListElementView());
    setFixedCellHeight(26);
    setCellRenderer(ModuleListElementView.CellRenderer);

    new ListSpeedSearch<>(this, ModuleListElementViewModel::getName);
  }

  @Override
  protected void updateElementView(@NotNull ModuleListElementView elementView,
                                   @NotNull ModuleListElementViewModel element) {
  }
}
