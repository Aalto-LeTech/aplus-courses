package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.PluginDependency;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PluginInstallerDialogs {

  private @NotNull String pluginListToString(@NotNull List<PluginDependency> pluginNames) {

  }

  public PluginInstallerCallback.ConsentResult askForInstallationConsentOnCreation(@NotNull List<PluginDependency> pluginNames) {
    return PluginInstallerCallback.ConsentResult.ACCEPTED;
  }

  private PluginInstallerDialogs() {

  }
}
