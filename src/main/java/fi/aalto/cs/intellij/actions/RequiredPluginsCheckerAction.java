package fi.aalto.cs.intellij.actions;

import static java.util.stream.Collectors.toMap;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO THINK HOW TO TEST & REFACTOR THIS
public class RequiredPluginsCheckerAction implements StartupActivity {

  private static final Logger logger = LoggerFactory.getLogger(RequiredPluginsCheckerAction.class);

  private static final Map<String, String> requiredPluginNames = new HashMap<>();
  private Map<String, String> missingOrDisabledPluginNames = new HashMap<>();
  private List<IdeaPluginDescriptor> missingOrDisabledIdeaPluginDescriptors;
  private List<IdeaPluginDescriptor> availableIdeaPluginDescriptors;

  @Override
  public void runActivity(@NotNull Project project) {
    requiredPluginNames.put("Scala", "org.intellij.scala");

    filterMissingOrDisabledPluginNames();
    createListOfMissingOrDisabledPluginDescriptors();
    checkPluginsStatusAndNotify();
  }

  /**
   * Filters out the plugin names that are missing from the current installation.
   */
  private void filterMissingOrDisabledPluginNames() {
    missingOrDisabledPluginNames = requiredPluginNames
        .entrySet()
        .stream()
        .filter(entry -> isPluginMissingOrDisabled(entry.getValue()))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  private static boolean isPluginMissingOrDisabled(String id) {
    PluginId pluginId = PluginId.getId(id);
    return !PluginManager.isPluginInstalled(pluginId) || PluginManager
        .isDisabled(pluginId.getIdString());
  }

  /**
   * If there any plugins missing, creates a list of the plugin descriptors for it based on the
   * publicly available ones in the JetBrains main plugin repository.
   */
  private void createListOfMissingOrDisabledPluginDescriptors() {
    if (missingOrDisabledPluginNames.size() > 0) {
      getAvailablePluginsFromMainRepo();
      missingOrDisabledIdeaPluginDescriptors = new ArrayList<>();
      missingOrDisabledPluginNames.forEach((name, id) -> {
        PluginId missingPluginId = PluginId.getId(id);
        availableIdeaPluginDescriptors
            .stream()
            .filter(
                availableDescriptor -> availableDescriptor.getPluginId().equals(missingPluginId)
            )
            .findFirst()
            .ifPresent(missingOrDisabledIdeaPluginDescriptors::add);
      });
    }
  }

  private void getAvailablePluginsFromMainRepo() {
    try {
      availableIdeaPluginDescriptors = RepositoryHelper.loadPlugins(new BgProgressIndicator());
    } catch (IOException ex) {
      logger.error("Could not retrieve plugins data from main repository.", ex);
    }
  }

  /**
   * Sorts required plugins into missing and disabled and shows respective notifications.
   */
  private void checkPluginsStatusAndNotify() {
    List<IdeaPluginDescriptor> missingPluginDescriptors = missingOrDisabledIdeaPluginDescriptors
        .stream()
        .filter(descriptor -> !PluginManager.isPluginInstalled(descriptor.getPluginId()))
        .collect(Collectors.toList());

    List<IdeaPluginDescriptor> disabledPluginDescriptors = missingPluginDescriptors
        .stream()
        .filter(IdeaPluginDescriptor::isEnabled)
        .collect(Collectors.toList());

    if (missingPluginDescriptors.size() > 0) {
      notifyAndSuggestPluginsInstallation(missingPluginDescriptors);
    } else if (disabledPluginDescriptors.size() > 0) {
      notifyAndSuggestPluginsEnabling(disabledPluginDescriptors);
    }
  }

  private void notifyAndSuggestPluginsEnabling(List<IdeaPluginDescriptor> descriptors) {
    Notification notification = new Notification(
        "A+",
        "A+",
        "Some plugins must be and enabled for the A+ plugin to work properly "
            + getPluginsNamesString(descriptors) + ".",
        NotificationType.WARNING);

    notification.addAction(new NotificationAction(
        "Enable the required plugin(s)" + getPluginsNamesString(descriptors) + ".") {

      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        descriptors.forEach(descriptor -> Objects
            .requireNonNull(PluginManager.getPlugin(descriptor.getPluginId())).setEnabled(true));
        notification.expire();
      }
    });

    Notifications.Bus.notify(notification);
  }

  private void notifyAndSuggestPluginsInstallation(List<IdeaPluginDescriptor> descriptors) {
    Notification notification = new Notification(
        "A+",
        "A+",
        "Additional plugin(s) must be installed and enabled for the A+ plugin to work "
            + "properly (" + getPluginsNamesString(descriptors) + ").",
        NotificationType.WARNING);

    notification.addAction(new NotificationAction(
        "Install missing (" + getPluginsNamesString(descriptors) + ") plugin(s).") {

      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        descriptors.forEach(descriptor -> {
          try {
            PluginDownloader pluginDownloader = PluginDownloader.createDownloader(descriptor);
            pluginDownloader.prepareToInstall(new BgProgressIndicator());
            pluginDownloader.install();
          } catch (IOException ex) {
            logger.error("Could not install plugin" + descriptor.getName() + ".", ex);
          }
        });
        PluginManagerConfigurable
            .showRestartDialog("Plugins required for A+ course have been now installed");
        notification.expire();
      }
    });

    Notifications.Bus.notify(notification);
  }

  @NotNull
  private StringJoiner getPluginsNamesString(List<IdeaPluginDescriptor> descriptors) {
    StringJoiner stringJoiner = new StringJoiner(", ");
    descriptors.forEach(descriptor -> stringJoiner.add(descriptor.getName()));
    return stringJoiner;
  }
}
