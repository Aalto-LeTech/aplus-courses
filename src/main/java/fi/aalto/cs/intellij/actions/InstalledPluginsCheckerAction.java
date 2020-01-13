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

import java.util.Arrays;

import static com.intellij.ide.plugins.PluginManager.*;

public class InstalledPluginsCheckerAction implements StartupActivity {

    final static private String[] requiredPluginNames = {"org.intellij.scala"};

    @Override
    public void runActivity(@NotNull Project project) {

        Arrays.stream(requiredPluginNames).forEach(
                requiredPluginName -> {
                    PluginId requiredPluginId = PluginId.getId(requiredPluginName);
                    boolean isPluginOK = !isPluginInstalled(requiredPluginId)
                            || isDisabled(requiredPluginId.getIdString());
                    if (isPluginOK) {
                        Notification notification = new Notification(
                                "A+",
                                "A+",
                                "<p>Plugin " + requiredPluginName +
                                        " must be installed and enabled for the A+ plugin to work properly.</p>",
                                NotificationType.WARNING);
                        notification.addAction(new NotificationAction("Search for the " + requiredPluginName + " plugin.") {
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
