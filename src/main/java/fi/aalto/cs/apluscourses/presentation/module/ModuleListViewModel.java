package fi.aalto.cs.apluscourses.presentation.module;

import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.presentation.base.BaseListViewModel;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModuleListViewModel extends BaseListViewModel<ModuleListElementViewModel> {

  private static final long serialVersionUID = -151101903512608512L;

  /**
   * Presentation model for a list of modules shown in the UI.
   * @param modules A list of modules (domain model objects).
   */
  public ModuleListViewModel(@NotNull List<Module> modules) {
    super(modules, ModuleListElementViewModel::new);
  }
}
