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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO THINK HOW TO TEST & REFACTOR THIS
public class RequiredPluginsCheckerAction implements StartupActivity {

  private static final Logger logger = LoggerFactory.getLogger(RequiredPluginsCheckerAction.class);

  private static final Map<String, String> requiredPluginNames = new HashMap<>();
  private static Map<String, String> missingOrDisabledPluginNames = new HashMap<>();
  private static List<IdeaPluginDescriptor> missingOrDisabledIdeaPluginDescriptors;
  private static List<IdeaPluginDescriptor> availableIdeaPluginDescriptors;

  /**
   * The plugin startup activity.
   */
  @Override
  public void runActivity(@NotNull Project project) {
    populateRequiredPluginNamesMap();
    filterMissingOrDisabledPluginNames();
    createListOfMissingOrDisabledPluginDescriptors();
    checkPluginsStatusAndNotify();
  }

  /**
   * Fills the list of required plugin names.
   *
   * Later, reading from the from configuration file might occur here.
   */
  private void populateRequiredPluginNamesMap() {
    requiredPluginNames.put("Scala", "org.intellij.scala");
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

  /**
   * Predicate for checking the plugin status.
   */
  private static boolean isPluginMissingOrDisabled(String id) {
    PluginId pluginId = PluginId.getId(id);
    return !PluginManager.isPluginInstalled(pluginId) || PluginManager
        .isDisabled(pluginId.getIdString());
  }

  /**
   * If there any plugins missing, creates a list of the plugin descriptors for them based on the
   * publicly available ones.
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

  /**
   * Get the list of all the available in the main JetBrains plugin repository.
   * <p>
   * Note! The descriptors for the not installed plugins do not exist in IJ until now.
   */
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
    List<IdeaPluginDescriptor> missingPluginDescriptors = new ArrayList<>();
    List<IdeaPluginDescriptor> disabledPluginDescriptors = new ArrayList<>();

    for (IdeaPluginDescriptor descriptor : missingOrDisabledIdeaPluginDescriptors) {
      if (!PluginManager.isPluginInstalled(descriptor.getPluginId())) {
        missingPluginDescriptors.add(descriptor);
      } else if (PluginManager.isDisabled(descriptor.getPluginId().getIdString())) {
        disabledPluginDescriptors.add(descriptor);
      }
    }

    if (missingPluginDescriptors.size() > 0) {
      notifyAndSuggestPluginsInstallation(missingPluginDescriptors);
    } else if (disabledPluginDescriptors.size() > 0) {
      notifyAndSuggestPluginsEnabling(disabledPluginDescriptors);
    }
  }

  /**
   * Notify with with an option to enable all the required plugins.
   */
  private void notifyAndSuggestPluginsEnabling(List<IdeaPluginDescriptor> descriptors) {
    Notification notification = new Notification(
        "A+",
        "A+",
        "Some plugins must be and enabled for the A+ plugin to work properly "
            + getPluginsNamesString(descriptors) + ".",
        NotificationType.WARNING);

    notification.addAction(new NotificationAction(
        "Enable the required plugin(s) (" + getPluginsNamesString(descriptors) + ").") {

      /**
       * Activate all the required plugins.
       */
      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        descriptors.forEach(descriptor -> Objects
            .requireNonNull(PluginManager.getPlugin(descriptor.getPluginId())).setEnabled(true));
        notification.expire();
      }
    });

    Notifications.Bus.notify(notification);
  }

  /**
   * Notify with with an option to install all the required plugins and suggest restart.
   */
  private void notifyAndSuggestPluginsInstallation(List<IdeaPluginDescriptor> descriptors) {
    Notification notification = new Notification(
        "A+",
        "A+",
        "Additional plugin(s) must be installed and enabled for the A+ plugin to work "
            + "properly (" + getPluginsNamesString(descriptors) + ").",
        NotificationType.WARNING);

    notification.addAction(new NotificationAction(
        "Install missing (" + getPluginsNamesString(descriptors) + ") plugin(s).") {

      /**
       * Install the missing plugins and propose a restart.
       */
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
        notification.expire();
        PluginManagerConfigurable
            .shutdownOrRestartApp("Plugins required for A+ course are now installed");
      }
    });

    Notifications.Bus.notify(notification);
  }

  /**
   * Join plugin descriptor names with a comma.
   */
  @NotNull
  private StringJoiner getPluginsNamesString(List<IdeaPluginDescriptor> descriptors) {
    StringJoiner stringJoiner = new StringJoiner(", ");
    descriptors.forEach(descriptor -> stringJoiner.add(descriptor.getName()));
    return stringJoiner;
  }
}
