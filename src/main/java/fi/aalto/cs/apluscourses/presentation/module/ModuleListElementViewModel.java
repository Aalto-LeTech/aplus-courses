package fi.aalto.cs.apluscourses.presentation.module;

import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.ListElementViewModel;
import java.awt.font.TextAttribute;
import org.jetbrains.annotations.NotNull;

public class ModuleListElementViewModel extends ListElementViewModel<Module> {

  public ModuleListElementViewModel(@NotNull Module module) {
    super(module);
    module.stateChanged.addListener(this, BaseViewModel::onChanged);
  }

  public String getName() {
    return getModel().getName();
  }

  public String getUrl() {
    return getModel().getUrl().toString();
  }

  /**
   * Returns a textual representation of the status of the module.
   * @return A {@link String} describing the status.
   */
  public String getStatus() {
    switch (getModel().stateMonitor.get()) {
      case Module.NOT_INSTALLED:
      case Module.FETCHED:
      case Module.UNLOADED:
      case Module.UNINSTALLED:
        return "Not installed";
      case Module.FETCHING:
        return "Downloading...";
      case Module.LOADING:
        return "Installing...";
      case Module.LOADED:
        return "Loaded";
      case Module.WAITING_FOR_DEPS:
        return "Waiting for dependencies...";
      case Module.INSTALLED:
        return "Installed";
      default:
        return "Error";
    }
  }

  /**
   * Returns a font weight in which the module is shown on a list.
   * @return A {@link Float} that can be set to font weight.
   */
  public float getFontWeight() {
    return getModel().stateMonitor.get() >= Module.LOADED
        ? TextAttribute.WEIGHT_BOLD
        : TextAttribute.WEIGHT_REGULAR;
  }
}
