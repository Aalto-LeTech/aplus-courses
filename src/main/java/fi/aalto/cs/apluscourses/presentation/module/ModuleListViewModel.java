package fi.aalto.cs.apluscourses.presentation.module;

import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.presentation.base.BaseListViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModuleListViewModel extends BaseListViewModel<ModuleListElementViewModel> {


  /**
   * Presentation model for a list of modules shown in the UI.
   *
   * @param modules A list of modules (domain model objects).
   */
  public ModuleListViewModel(@NotNull List<Module> modules, @NotNull Options options) {
    super(modules, options, ModuleListElementViewModel::new);
  }

  public boolean canOpenDocumentation() {
    return getSingleSelectedElement().filter(ModuleListElementViewModel::canOpenDocumentation).isPresent();
  }

}

