package fi.aalto.cs.intellij.activities;

import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.createListOfMissingOrDisabledPluginDescriptors;
import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.filterDisabledPluginDescriptors;
import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.filterMissingOrDisabledPluginNames;
import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.filterMissingPluginDescriptors;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.intellij.actions.EnablePluginsNotificationAction;
import fi.aalto.cs.intellij.actions.InstallPluginsNotificationAction;
import fi.aalto.cs.intellij.notifications.EnablePluginsNotification;
import fi.aalto.cs.intellij.notifications.InstallPluginsNotification;
import fi.aalto.cs.intellij.services.PluginSettings;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/**
 * A startup activity that checks and hints on missing or disabled required for the course plugins.
 */
public class RequiredPluginsCheckerActivity implements StartupActivity {

  /**
   * An actual startup work to filter out invalid (missing or disabled) plugins and notify gets done
   * here.
   *
   * @param project is a {@link Project} object for the current project.
   */
  @Override
  public void runActivity(@NotNull Project project) {
    List<IdeaPluginDescriptor> missingOrDisabledIdeaPluginDescriptors =
        getActualListOfMissingOrDisabledIdeaPluginDescriptors();
    if (!missingOrDisabledIdeaPluginDescriptors.isEmpty()) {
      checkPluginsStatusAndNotify(missingOrDisabledIdeaPluginDescriptors);
    }
  }

  /**
   * A method to fetch data for required plugins and check their validity.
   *
   * @return a {@link List} of {@link IdeaPluginDescriptor} that are missing or disabled.
   */
  @NotNull
  private List<IdeaPluginDescriptor> getActualListOfMissingOrDisabledIdeaPluginDescriptors() {
    Map<String, String> requiredPlugins = PluginSettings
        .getInstance()
        .getCurrentlyLoadedCourse()
        .getRequiredPlugins();
    Map<String, String> missingOrDisabledPlugins
        = filterMissingOrDisabledPluginNames(requiredPlugins);
    return createListOfMissingOrDisabledPluginDescriptors(missingOrDisabledPlugins);
  }

  /**
   * Sorts required plugins into missing and disabled and shows respective notifications.
   *
   * @param missingOrDisabledIdeaPluginDescriptors a {@link List} of {@link IdeaPluginDescriptor}
   *                                               that are invalid.
   */
  private void checkPluginsStatusAndNotify(
      List<IdeaPluginDescriptor> missingOrDisabledIdeaPluginDescriptors) {
    List<IdeaPluginDescriptor> missingPluginDescriptors = filterMissingPluginDescriptors(
        missingOrDisabledIdeaPluginDescriptors);
    List<IdeaPluginDescriptor> disabledPluginDescriptors = filterDisabledPluginDescriptors(
        missingOrDisabledIdeaPluginDescriptors);

    if (!missingPluginDescriptors.isEmpty()) {
      notifyAndSuggestPluginsInstallation(missingPluginDescriptors);
    } else if (!disabledPluginDescriptors.isEmpty()) {
      notifyAndSuggestPluginsEnabling(disabledPluginDescriptors);
    }
  }

  /**
   * Notify with an option to enable all the required plugins.
   *
   * @param disabledPluginDescriptors a {@link List} of disabled {@link IdeaPluginDescriptor} to
   *                                  enable.
   */
  private void notifyAndSuggestPluginsEnabling(
      List<IdeaPluginDescriptor> disabledPluginDescriptors) {
    Notification notification = new EnablePluginsNotification(disabledPluginDescriptors);
    notification.addAction(new EnablePluginsNotificationAction(disabledPluginDescriptors));
    Notifications.Bus.notify(notification);
  }

  /**
   * Notify with an option to install all the required plugins and suggest restart.
   *
   * @param missingPluginDescriptors a {@link List} of missing {@link IdeaPluginDescriptor} to
   *                                 download and install.
   */
  private void notifyAndSuggestPluginsInstallation(
      List<IdeaPluginDescriptor> missingPluginDescriptors) {
    Notification notification = new InstallPluginsNotification(missingPluginDescriptors);
    notification.addAction(new InstallPluginsNotificationAction(missingPluginDescriptors));
    Notifications.Bus.notify(notification);
  }
}
