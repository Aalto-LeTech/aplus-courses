package fi.aalto.cs.intellij.actions;

import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.getPluginsNamesString;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.ide.plugins.newui.BgProgressIndicator;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper on {@link NotificationAction} to interactively <b>activate</b> provided plugins.
 */
public class InstallPluginsNotificationAction extends NotificationAction {

  private final Logger logger = LoggerFactory
      .getLogger(InstallPluginsNotificationAction.class);

  private List<IdeaPluginDescriptor> missingIdeaPluginDescriptors;
  private PluginInstaller pluginInstaller;
  private RestartProposer restartProposer;

  /**
   * Builds the action.
   *
   * @param missingIdeaPluginDescriptors is a {@link List} of {@link IdeaPluginDescriptor} that can
   *                                     be installed.
   */
  public InstallPluginsNotificationAction(List<IdeaPluginDescriptor> missingIdeaPluginDescriptors) {
    super("Install missing ("
        + getPluginsNamesString(missingIdeaPluginDescriptors) + ") plugin(s).");
    this.missingIdeaPluginDescriptors = missingIdeaPluginDescriptors;
    this.pluginInstaller = InstallPluginsNotificationAction::install;
    this.restartProposer = InstallPluginsNotificationAction::proposeRestart;
  }

  /**
   * Builds the action.
   *
   * @param missingIdeaPluginDescriptors is a {@link List} of {@link IdeaPluginDescriptor} that can
   *                                     be installed.
   */
  public InstallPluginsNotificationAction(
      List<IdeaPluginDescriptor> missingIdeaPluginDescriptors,
      PluginInstaller pluginInstaller,
      RestartProposer restartProposer) {
    super("Install missing ("
        + getPluginsNamesString(missingIdeaPluginDescriptors) + ") plugin(s).");
    this.missingIdeaPluginDescriptors = missingIdeaPluginDescriptors;
    this.pluginInstaller = pluginInstaller;
    this.restartProposer = restartProposer;
  }

  /**
   * Install missing plugins and propose a restart.
   *
   * @param e            an {@link AnActionEvent}.
   * @param notification a {@link Notification} to handle.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    //todo what if all the plugins are dynamic? (no way, I know)
    // what if the plugins are not installed at all?
    missingIdeaPluginDescriptors.forEach(descriptor -> {
      try {
        pluginInstaller.install(descriptor);
      } catch (IOException ex) {
        logger.error("Could not install plugin" + descriptor.getName() + ".", ex);
      }
    });
    notification.expire();
    restartProposer.proposeRestart();
  }

  /**
   * A method responsible for plugin installation.
   */
  public static void install(IdeaPluginDescriptor descriptor) throws IOException {
    PluginDownloader pluginDownloader = PluginDownloader.createDownloader(descriptor);
    pluginDownloader.prepareToInstall(new BgProgressIndicator());
    pluginDownloader.install();
  }

  /**
   * A method to propose a restart when the plugins have been installed.
   */
  public static void proposeRestart() {
    PluginManagerConfigurable
        .shutdownOrRestartApp("Plugins required for A+ course are now installed.");
  }

  /**
   * An abstract interface for an object that installs plugins based on the {@link
   * IdeaPluginDescriptor} provided. The most useful realization of this interface is {@code
   * InstallPluginsNotificationAction::install}.
   */
  @FunctionalInterface
  public interface PluginInstaller {

    /**
     * Installs a given {@link IdeaPluginDescriptor}.
     *
     * @param descriptor is the {@link IdeaPluginDescriptor} of the desired plugin.
     */
    void install(IdeaPluginDescriptor descriptor) throws IOException;
  }
}
