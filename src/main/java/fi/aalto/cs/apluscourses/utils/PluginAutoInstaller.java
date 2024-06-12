package fi.aalto.cs.apluscourses.utils;

import com.intellij.ide.plugins.PluginEnabler;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.plugins.PluginManagerMain;
import com.intellij.ide.plugins.PluginNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.ui.utils.PluginInstallerCallback;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginAutoInstaller {

  private static class PluginInstallerWorker implements ThrowableComputable<Boolean, RuntimeException> {

    private final @Nullable Project project;

    private final @Nullable Notifier notifier;

    private final @NotNull List<PluginNode> downloadablePluginNodes;

    private PluginInstallerWorker(@Nullable Project project,
                                  @Nullable Notifier notifier,
                                  @NotNull List<PluginNode> downloadablePluginNodes) {
      this.project = project;
      this.notifier = notifier;
      this.downloadablePluginNodes = downloadablePluginNodes;
    }

    @Override
    public Boolean compute() {
      final var indicator = Objects.requireNonNull(ProgressManager.getInstance().getProgressIndicator());
      indicator.setIndeterminate(true);

      // The semaphore must be released (i.e. permits must be raised) once per each plugin installed.
      // Only when the semaphore's permit count reaches 1, this function can return.
      final Semaphore lock = new Semaphore(-downloadablePluginNodes.size() + 1);

      // "true" means that installation is successful (so far)
      AtomicBoolean installationResult = new AtomicBoolean(true);

      ApplicationManager.getApplication().invokeLater(() -> {
        try {
          final Consumer<Boolean> onInstallationComplete = (result) -> {
            installationResult.set(installationResult.get() & result);
            lock.release();
          };

          WriteAction.run(() -> PluginManagerMain.downloadPlugins(downloadablePluginNodes, Collections.emptyList(),
              false, null, PluginEnabler.getInstance(), ModalityState.defaultModalityState(), onInstallationComplete));
        } catch (IOException ex) {
          if (notifier != null) {
            notifier.notify(new NetworkErrorNotification(ex), project);
          }

          installationResult.set(false);
        }
      });

      for (; ; ) {
        // Semaphore acquisition will succeed once all plugins have been installed.
        if (lock.tryAcquire()) {
          // If plugin installation failed at any point (for example: an attempt to install a non-existent plugin),
          // we return null to indicate a failed operation.
          return installationResult.get() ? false : null;
        }

        // This will throw an exception if a user has cancelled the operation. It won't actually cancel
        // the installation of the current plugin; that would corrupt the IDE. But it will at least return
        // from this function and close the progress bar.
        indicator.checkCanceled();

        // In practice, an interruption should never happen.
        try {
          Thread.sleep(50);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          return null;
        }
      }
    }
  }

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
   *
   * @param project     The project which initiated the operation.
   * @param notifier    A notifier instance, used to show a notification if something goes wrong.
   * @param pluginNames List of plugin ID strings. The plugin ID can be found on the plugin's
   *                    JetBrains Marketplace website.
   * @param callback    An interface implementing functions invoked by this method. The functions
   *                    are related to showing the consent dialog box to the user.
   * @return True, if all dependencies are satisfied and there's nothing to be done. False, if
   * some dependencies were missing, and they were installed or enabled. Null, if the operation
   * has been cancelled due to missing consent or an error.
   */
  @RequiresEdt
  public static Boolean ensureDependenciesInstalled(@Nullable Project project,
                                                    @Nullable Notifier notifier,
                                                    @NotNull List<PluginDependency> pluginNames,
                                                    @NotNull PluginInstallerCallback callback) {
    final var pluginIDs = pluginNames.stream().map(p -> PluginId.getId(p.getId())).collect(Collectors.toSet());

    // Downloading and enabling plugins are different operations, so we handle these separately.
    final var pluginsToDownload = pluginIDs.stream()
        .filter(PluginAutoInstaller::shouldDownloadPlugin).collect(Collectors.toSet());
    final var pluginsToEnable = pluginIDs.stream()
        .filter(PluginAutoInstaller::shouldEnablePlugin).collect(Collectors.toSet());

    pluginsToDownload.forEach(id -> logger.info("Plugin to download: %s".formatted(id.getIdString())));
    pluginsToEnable.forEach(id -> logger.info("Plugin to enable: %s".formatted(id.getIdString())));

    if (pluginsToDownload.isEmpty() && pluginsToEnable.isEmpty()) {
      return true;
    }

    // allPluginNames = list of PluginDependencies for all plugins in pluginsToDownload or pluginsToEnable
    final var missingPluginIds = Stream.concat(pluginsToDownload.stream(), pluginsToEnable.stream())
        .map(PluginId::getIdString).collect(Collectors.toSet());
    final var allPluginNames = pluginNames.stream()
        .filter(p -> missingPluginIds.contains(p.getId()))
        .collect(Collectors.toList());

    final var consentResult = callback.askForInstallationConsent(allPluginNames);
    if (consentResult == PluginInstallerCallback.ConsentResult.IGNORE_INSTALL) {
      return true; // pretend that everything went fine
    } else if (consentResult == PluginInstallerCallback.ConsentResult.REJECTED) {
      return null; // signal to the caller to abort the operation
    }

    // Enable all requires plugins that are installed, but disabled.
    PluginEnabler.getInstance().enableById(pluginsToEnable);

    final var downloadablePluginNodes = pluginsToDownload.stream()
        .map(PluginNode::new).collect(Collectors.toList());

    final var installerWorker = new PluginInstallerWorker(project, notifier, downloadablePluginNodes);

    return ProgressManager.getInstance().runProcessWithProgressSynchronously(installerWorker,
        "Downloading required plugins", true, project);
  }

  private PluginAutoInstaller() {

  }
}
