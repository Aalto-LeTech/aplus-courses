package fi.aalto.cs.intellij.presentation.module;

import fi.aalto.cs.intellij.model.Module;
import fi.aalto.cs.intellij.presentation.base.BaseListModel;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ModuleListModel extends BaseListModel<ModuleListElementModel> {

  public ModuleListModel(@NotNull List<Module> modules) {
    super(modules
        .stream()
        .map(ModuleListElementModel::new)
        .collect(Collectors.toList()));
  }
}
