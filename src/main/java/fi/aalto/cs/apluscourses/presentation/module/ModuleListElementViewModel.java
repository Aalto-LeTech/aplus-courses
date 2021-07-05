package fi.aalto.cs.apluscourses.presentation.module;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.ListElementViewModel;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import org.jetbrains.annotations.NotNull;

public class ModuleListElementViewModel extends ListElementViewModel<Module>
        implements Searchable {

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

  @Override
  public @NotNull String getSearchableString() {
    return getName();
  }

  /**
   * Returns the changelog if it's defined and an update is available,
   * else the timestamp of the module if it's defined, else the URL.
   *
   * @return A {@link String} with info about the module.
   */
  public String getTooltip() {
    var timestamp = getModel().getMetadata().getDownloadedAt();
    var changelog = getModel().getChangelog();
    if (isUpdateAvailable() && !changelog.equals("")) {
      return getAndReplaceText("presentation.moduleTooltip.changelog", changelog);
    } else if (timestamp != null) {
      return getAndReplaceText("presentation.moduleTooltip.timestamp",
          timestamp.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
    }
    return getAndReplaceText("presentation.moduleTooltip.moduleURL", getUrl());
  }

  public boolean isUpdateAvailable() {
    return getModel().isUpdatable();
  }

  @NotNull
  private String getErrorStatus() {
    switch (getModel().getErrorCause()) {
      case Component.ERR_FILES_MISSING:
        return getText("presentation.moduleStatuses.errorFilesMissing");
      default:
        return getText("presentation.moduleStatuses.errorUnknown");
    }
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
      case Component.FETCHED:
        return getText("presentation.moduleStatuses.doubleClickToInstall");
      case Component.FETCHING:
        return getText("presentation.moduleStatuses.downloading");
      case Component.LOADING:
        return getText("presentation.moduleStatuses.installing");
      case Component.LOADED:
        break;
      case Component.UNINSTALLING:
        return getText("presentation.moduleStatuses.uninstalling");
      case Component.UNINSTALLED:
        return getText("presentation.moduleStatuses.uninstalled");
      case Component.ACTION_ABORTED:
        return getText("presentation.moduleStatuses.cancelling");
      default:
        return getErrorStatus();
    }
    switch (model.dependencyStateMonitor.get()) {
      case Component.DEP_INITIAL:
        return getText("presentation.dependencyStatus.installedDependenciesUnknown");
      case Component.DEP_WAITING:
        return getText("presentation.dependencyStatus.waitingForDependencies");
      case Component.DEP_LOADED:
        return getText("presentation.dependencyStatus.installed");
      default:
        return getText("presentation.dependencyStatus.errorInDependencies");
    }
  }

  /**
   * Indicates whether the module shown on a list should be displayed bold-faced.
   */
  public boolean isBoldface() {
    Module model = getModel();
    return !model.hasError() && model.stateMonitor.get() == Component.LOADED;
  }
}
