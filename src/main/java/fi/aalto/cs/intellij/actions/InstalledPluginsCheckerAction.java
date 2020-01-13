package fi.aalto.cs.intellij.actions;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.ide.plugins.PluginManager.*;

public class InstalledPluginsCheckerAction implements StartupActivity {

    final static private Map<String, String> requiredPluginNames = new HashMap<>();

    @Override
    public void runActivity(@NotNull Project project) {

        requiredPluginNames.put("Scala", "org.intellij.scala");

        requiredPluginNames.forEach((key, value) -> {
                    PluginId requiredPluginId = PluginId.getId(value);
                    boolean isPluginOK = !isPluginInstalled(requiredPluginId)
                            || isDisabled(requiredPluginId.getIdString());
                    if (isPluginOK) {
                        Notification notification = new Notification(
                                "A+",
                                "A+",
                                "<p>Plugin " + value +
                                        " must be installed and enabled for the A+ plugin to work properly.</p>",
                                NotificationType.WARNING);
                        notification.addAction(new NotificationAction("Search for the " + key + " plugin.") {
                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                Project project = e.getProject();
                                ShowSettingsUtil settingsUtil = ShowSettingsUtilImpl.getInstance();
                                settingsUtil.showSettingsDialog(project, "Plugins");
                            }
                        });
                        Notifications.Bus.notify(notification);
                    }
                }
        );
    }
}
