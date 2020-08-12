package fi.aalto.cs.apluscourses.presentation.module;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

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

  public Boolean isUpdateAvailable() {
    return getModel().isUpdatable();
  }

  /**
   * Returns a textual representation of the status of the module.
   * @return A {@link String} describing the status.
   */
  public String getStatus() {
    Module model = getModel();
    switch (model.stateMonitor.get()) {
      case Component.UNRESOLVED:
        return getText("presentation.moduleStatuses.unknown");
      case Component.NOT_INSTALLED:
        return getText("presentation.moduleStatuses.notInstalled");
      case Component.FETCHING:
        return getText("presentation.moduleStatuses.fetching");
      case Component.FETCHED:
        return getText("presentation.moduleStatuses.fetched");
      case Component.LOADING:
        return getText("presentation.moduleStatuses.loading");
      case Component.LOADED:
        break;
      case Component.UNINSTALLING:
        return getText("presentation.moduleStatuses.uninstalling");
      case Component.UNINSTALLED:
        return getText("presentation.moduleStatuses.uninstalled");
      case Component.ACTION_ABORTED:
        return getText("presentation.moduleStatuses.actionAborted");
      default:
        return getText("presentation.moduleStatuses.error");
    }
    switch (model.dependencyStateMonitor.get()) {
      case Component.DEP_INITIAL:
        return getText("presentation.dependencyStatus.depInitial");
      case Component.DEP_WAITING:
        return getText("presentation.dependencyStatus.depWaiting");
      case Component.DEP_LOADED:
        return getText("presentation.dependencyStatus.depLoaded");
      default:
        return getText("presentation.dependencyStatus.depError");
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
