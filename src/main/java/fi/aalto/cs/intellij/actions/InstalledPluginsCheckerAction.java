package fi.aalto.cs.intellij.actions;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class InstalledPluginsCheckerAction implements StartupActivity {

    final static private String[] requiredPluginNames = {"org.intellij.scala"};

    @Override
    public void runActivity(@NotNull Project project) {

        Arrays.stream(requiredPluginNames).forEach(
                requiredPluginName -> {
                    PluginId requiredPluginId = PluginId.getId(requiredPluginName);
                    if (!PluginManager.isPluginInstalled(requiredPluginId)) {
                        Notifications.Bus.notify(new Notification(
                                "A+",
                                "A+",
                                "Plugin " + requiredPluginName + " is required for the A+ plugin to work properly.",
                                NotificationType.WARNING));
                    }
                }
        );
    }
}
