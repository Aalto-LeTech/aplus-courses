package fi.aalto.cs.intellij.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.extensions.PluginId;
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
                        Notifications.Bus.notify(new Notification(
                                "A+",
                                "A+",
                                "Plugin " + requiredPluginName +
                                        " must be installed and enabled for the A+ plugin to work properly.",
                                NotificationType.WARNING));
                    }
                }
        );
    }
}
