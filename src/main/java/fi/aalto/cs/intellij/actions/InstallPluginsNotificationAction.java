package fi.aalto.cs.intellij.actions;

import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.getPluginsNamesString;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.ide.plugins.newui.BgProgressIndicator;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import fi.aalto.cs.intellij.activities.RequiredPluginsCheckerActivity;
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
      .getLogger(RequiredPluginsCheckerActivity.class);

  private List<IdeaPluginDescriptor> missingIdeaPluginDescriptors;

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
  }

  /**
   * Install missing plugins and propose a restart.
   *
   * @param e            an {@link AnActionEvent}.
   * @param notification a {@link Notification} to handle.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    missingIdeaPluginDescriptors.forEach(descriptor -> {
      //todo what if all the plugins are dynamic? (no way, I know)
      try {
        PluginDownloader pluginDownloader = PluginDownloader.createDownloader(descriptor);
        pluginDownloader.prepareToInstall(new BgProgressIndicator());
        pluginDownloader.install();
      } catch (IOException ex) {
        logger.error("Could not install plugin" + descriptor.getName() + ".", ex);
      }
    });
    notification.expire();
    PluginManagerConfigurable
        .shutdownOrRestartApp("Plugins required for A+ course are now installed.");
  }
}
