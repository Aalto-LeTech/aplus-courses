package fi.aalto.cs.apluscourses.presentation.module;

import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.presentation.base.BaseListViewModel;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ModuleListViewModel extends BaseListViewModel<ModuleListElementViewModel> {

  /**
   * Presentation model for a list of modules shown in the UI.
   * @param modules A list of modules (domain model objects).
   */
  public ModuleListViewModel(@NotNull List<Module> modules) {
    super(modules
        .stream()
        .map(ModuleListElementViewModel::new)
        .collect(Collectors.toList()));
  }
}
