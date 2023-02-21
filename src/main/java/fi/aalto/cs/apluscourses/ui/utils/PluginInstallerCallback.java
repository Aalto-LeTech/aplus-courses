package fi.aalto.cs.apluscourses.ui.utils;

import java.util.List;

@FunctionalInterface
public interface PluginInstallerCallback {
  enum ConsentResult
  {
    REJECTED, // User refused, fail the operation
    ACCEPTED, // User accepted, install plugins and succeed
    IGNORE_INSTALL // User refused, skip installation and return success anyway
  }

  ConsentResult askForInstallationConsent(List<String> pluginNames);
}
