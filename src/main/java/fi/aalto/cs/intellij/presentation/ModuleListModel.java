package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.Module;
import fi.aalto.cs.intellij.presentation.common.BaseListModel;
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
