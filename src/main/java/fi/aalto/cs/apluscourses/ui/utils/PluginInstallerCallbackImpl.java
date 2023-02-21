package fi.aalto.cs.apluscourses.ui.utils;

import java.util.List;

public class PluginInstallerCallbackImpl implements PluginInstallerCallback {
  @Override
  public ConsentResult askForInstallationConsent(List<String> pluginNames) {
    return ConsentResult.ACCEPTED;
  }
}
