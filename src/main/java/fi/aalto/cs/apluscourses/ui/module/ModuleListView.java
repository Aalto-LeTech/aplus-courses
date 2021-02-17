package fi.aalto.cs.apluscourses.ui.module;

import com.intellij.ui.ListSpeedSearch;
import fi.aalto.cs.apluscourses.presentation.module.ModuleListElementViewModel;
import fi.aalto.cs.apluscourses.ui.base.BaseListView;

public class ModuleListView extends BaseListView<ModuleListElementViewModel> {
  /**
   * Constructs a view for the module list.
   */
  public ModuleListView() {
    setFixedCellHeight(26);
    setCellRenderer(new ColoredModuleListRenderer());

    new ListSpeedSearch<>(this, ModuleListElementViewModel::getSearchableString);
  }
}
