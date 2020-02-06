package fi.aalto.cs.intellij.presentation.module;

import fi.aalto.cs.intellij.model.Module;
import fi.aalto.cs.intellij.presentation.base.BaseListModel;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ModuleListModel extends BaseListModel<ModuleListElementModel> {

  /**
   * Presentation model for a list of modules shown in the UI.
   * @param modules A list of modules (domain model objects).
   */
  public ModuleListModel(@NotNull List<Module> modules) {
    super(modules
        .stream()
        .map(ModuleListElementModel::new)
        .collect(Collectors.toList()));
  }
}
