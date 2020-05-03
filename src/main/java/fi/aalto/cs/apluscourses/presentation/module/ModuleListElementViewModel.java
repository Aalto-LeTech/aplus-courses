package fi.aalto.cs.apluscourses.presentation.module;

import fi.aalto.cs.apluscourses.model.Component;
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
    Module model = getModel();
    switch (model.stateMonitor.get()) {
      case Component.UNRESOLVED:
        return "Unknown";
      case Component.NOT_INSTALLED:
        return "Double-click to download";
      case Component.FETCHING:
        return "Downloading...";
      case Component.FETCHED:
        return "Double-click to install";
      case Component.LOADING:
        return "Installing...";
      case Component.LOADED:
        break;
      default:
        return "Error";
    }
    switch (model.dependencyStateMonitor.get()) {
      case Component.DEP_INITIAL:
        return "Installed, dependencies unknown";
      case Component.DEP_WAITING:
        return "Waiting for dependencies...";
      case Component.DEP_LOADED:
        return "Installed";
      default:
        return "Error in dependencies";
    }
  }

  /**
   * Returns a font weight in which the module is shown on a list.
   * @return A {@link Float} that can be set to font weight.
   */
  public float getFontWeight() {
    Module model = getModel();
    return !model.hasError() && model.stateMonitor.get() == Component.LOADED
        ? TextAttribute.WEIGHT_BOLD
        : TextAttribute.WEIGHT_REGULAR;
  }
}
