package fi.aalto.cs.apluscourses.utils;

import com.intellij.ide.plugins.PluginEnabler;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.plugins.PluginManagerMain;
import com.intellij.ide.plugins.PluginNode;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.ui.utils.PluginInstallerCallback;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PluginAutoInstaller {

  private static final Logger logger = APlusLogger.logger;

  private static boolean shouldDownloadPlugin(@NotNull PluginId id) {
    return !PluginManagerCore.isPluginInstalled(id);
  }

  private static boolean shouldEnablePlugin(@NotNull PluginId id) {
    final var plugin = PluginManagerCore.getPlugin(id);
    return plugin != null && !plugin.isEnabled();
  }

  /**
   * Verifies that all plugins with given plugin IDs are installed and enabled. If not, the function
   * asks the user for consent to install and enable all missing plugins.
   * @param project The project which initiated the operation.
   * @param notifier A notifier instance, used to show a notification if something goes wrong.
   * @param pluginNames List of plugin ID strings. The plugin ID can be found on the plugin's
   *                    JetBrains Marketplace website.
   * @param callback An interface implementing functions invoked by this method. The functions
   *                 are related to showing the consent dialog box to the user.
   * @return True, if all dependencies are satisfied and there's nothing to be done. False, if
   * some dependencies were missing, and they were installed or enabled. Null, if the operation
   * has been cancelled due to missing consent or an error.
   */
  @RequiresEdt
  public static Boolean ensureDependenciesInstalled(@Nullable Project project,
                                                    @Nullable Notifier notifier,
                                                    @NotNull List<PluginDependency> pluginNames,
                                                    @NotNull PluginInstallerCallback callback) {
    final var pluginIDs = pluginNames.stream().map(p -> PluginId.getId(p.getId())).collect(Collectors.toList());

    // Downloading and enabling plugins are different operations, so we handle these separately.
    final var pluginsToDownload = pluginIDs.stream()
        .filter(PluginAutoInstaller::shouldDownloadPlugin).collect(Collectors.toList());
    final var pluginsToEnable = pluginIDs.stream()
        .filter(PluginAutoInstaller::shouldEnablePlugin).collect(Collectors.toList());

    pluginsToDownload.forEach(id -> logger.info("Plugin to download: {}", id.getIdString()));
    pluginsToEnable.forEach(id -> logger.info("Plugin to enable: {}", id.getIdString()));

    if (pluginsToDownload.isEmpty() && pluginsToEnable.isEmpty()) {
      return true;
    }

    // allPluginNames = list of PluginDependencies for all plugins in pluginsToDownload or pluginsToEnable
    final var missingPluginsStream = Stream.concat(pluginsToDownload.stream(), pluginsToEnable.stream());
    final var allPluginNames = pluginNames.stream()
        .filter(p -> missingPluginsStream.anyMatch(id -> id.getIdString().equals(p.getId())))
        .collect(Collectors.toList());

    final var consentResult = callback.askForInstallationConsent(allPluginNames);
    if (consentResult == PluginInstallerCallback.ConsentResult.IGNORE_INSTALL) {
      return true; // pretend that everything went fine
    } else if (consentResult == PluginInstallerCallback.ConsentResult.REJECTED) {
      return null; // signal to the caller to abort the operation
    }

    // Enable all requires plugins that are installed, but disabled.
    pluginsToEnable.forEach(PluginManagerCore::enablePlugin);

    final var downloadablePluginNodes = pluginsToDownload.stream()
        .map(PluginNode::new).collect(Collectors.toList());

    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      final var indicator = Objects.requireNonNull(ProgressManager.getInstance().getProgressIndicator());
      indicator.setIndeterminate(true);

      // The semaphore must be released (i.e. permits must be raised) once per each plugin installed.
      // Only when the semaphore's permit count reaches 1, this function can return.
      final Semaphore lock = new Semaphore(-downloadablePluginNodes.size() + 1);

      try {
        PluginManagerMain.downloadPlugins(downloadablePluginNodes, Collections.emptyList(), true, null,
            PluginEnabler.getInstance(), ModalityState.defaultModalityState(), (res) -> lock.release());
      } catch (IOException ex) {
        if (notifier != null) {
          notifier.notify(new NetworkErrorNotification(ex), project);
        }

        return null;
      }

      for (;;) {
        // Semaphore acquisition will succeed once all plugins have been installed.
        if (lock.tryAcquire()) {
          return false;
        }

        // This will throw an exception if a user has cancelled the operation. It won't actually cancel
        // the installation of the current plugin, that would corrupt the IDE.
        indicator.checkCanceled();

        // In practice, an interruption should never happen.
        try {
          Thread.sleep(50);
        } catch (InterruptedException ex) {
          return null;
        }
      }

    }, "Downloading required plugins", true, project);

    return false;
  }

  private PluginAutoInstaller() {

  }
}
