package fi.aalto.cs.intellij.actions;

import static com.intellij.ide.plugins.PluginManager.isDisabled;
import static com.intellij.ide.plugins.PluginManager.isPluginInstalled;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class RequiredPluginsCheckerAction implements StartupActivity {

  final static private Map<String, String> requiredPluginNames = new HashMap<>();

  @Override
  public void runActivity(@NotNull Project project) {
    requiredPluginNames.put("Scala", "org.intellij.scala");
    requiredPluginNames.forEach(this::process);
  }

  private void process(String key, String value) {
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
        Project project = e.getProject();
        ShowSettingsUtil settingsUtil = ShowSettingsUtilImpl.getInstance();
        PluginManagerConfigurable pluginManagerConfigurable = new PluginManagerConfigurable();
        settingsUtil.editConfigurable(project, pluginManagerConfigurable);
      }
    });

    Notifications.Bus.notify(notification);
  }
}
