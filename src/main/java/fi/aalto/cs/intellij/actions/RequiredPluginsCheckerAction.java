package fi.aalto.cs.intellij.actions;

import static com.intellij.ide.plugins.PluginManager.isDisabled;
import static com.intellij.ide.plugins.PluginManager.isPluginInstalled;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.ide.plugins.RepositoryHelper;
import com.intellij.ide.plugins.newui.BgProgressIndicator;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO REFACTOR THIS MESS
public class RequiredPluginsCheckerAction implements StartupActivity {

  private static final Map<String, String> requiredPluginNames = new HashMap<>();
  private IdeaPluginDescriptor descriptor;

  @Override
  public void runActivity(@NotNull Project project) {

    try {
      descriptor = getIdeaPluginDescriptor(PluginId.getId("org.intellij.scala"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    requiredPluginNames.put("Scala", "org.intellij.scala");
    requiredPluginNames.forEach(this::checkPluginStatus);
  }

  private void checkPluginStatus(String key, String value) {
    PluginId requiredPluginId = PluginId.getId(value);
    if (!isPluginInstalled(requiredPluginId)) {
      notifyAndOpenPluginSettingsWindow(key, value);
    } else if (isDisabled(requiredPluginId.getIdString())) {
      notifyAndSuggestPluginEnabling(key, value, requiredPluginId);
    }
  }

  private void notifyAndSuggestPluginEnabling(String key, String value, PluginId requiredPluginId) {
    Notification notification = new Notification(
        "A+",
        "A+",
        "Plugin " + value
            + " must be and enabled for the A+ plugin to work properly.",
        NotificationType.WARNING);
    notification.addAction(new NotificationAction("Enable the " + key + " plugin.") {

      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        Objects.requireNonNull(PluginManager.getPlugin(requiredPluginId)).setEnabled(true);
        notification.expire();
      }
    });

    Notifications.Bus.notify(notification);
  }

  private void notifyAndOpenPluginSettingsWindow(String key, String value) {
    Notification notification = new Notification(
        "A+",
        "A+",
        "Plugin " + value
            + " must be installed and enabled for the A+ plugin to work properly.",
        NotificationType.WARNING);

    notification.addAction(new NotificationAction("Search for the " + key + " plugin.") {

      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        try {
          PluginDownloader pluginDownloader = PluginDownloader.createDownloader(descriptor);
          pluginDownloader.prepareToInstall(new BgProgressIndicator());
          pluginDownloader.install();
          PluginManagerConfigurable.showRestartDialog("Plugins required for A+ course have been now installed");
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    });

    Notifications.Bus.notify(notification);
  }

  @Nullable
  private IdeaPluginDescriptor getIdeaPluginDescriptor(PluginId requiredPluginId)
      throws IOException {
    IdeaPluginDescriptor ideaPluginDescriptor;
    List<IdeaPluginDescriptor> ideaPluginDescriptors = RepositoryHelper.loadPlugins(null);
    ideaPluginDescriptor = ideaPluginDescriptors
        .stream()
        .filter(desc -> desc.getPluginId().equals(requiredPluginId))
        .findFirst()
        .orElse(null);
    return ideaPluginDescriptor;
  }
}
