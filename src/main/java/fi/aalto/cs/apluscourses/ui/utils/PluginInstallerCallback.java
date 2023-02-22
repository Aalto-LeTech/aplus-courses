package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.PluginDependency;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PluginInstallerCallback {
  enum ConsentResult
  {
    REJECTED, // User refused, fail the operation
    ACCEPTED, // User accepted, install plugins and succeed
    IGNORE_INSTALL // User refused, skip installation and return success anyway
  }

  ConsentResult askForInstallationConsent(@NotNull List<PluginDependency> pluginNames);
}
