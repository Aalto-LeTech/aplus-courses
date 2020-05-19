package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.intellij.utils.RequiredPluginsCheckerUtil.createListOfMissingOrDisabledPluginDescriptors;
import static fi.aalto.cs.apluscourses.intellij.utils.RequiredPluginsCheckerUtil.filterDisabledPluginDescriptors;
import static fi.aalto.cs.apluscourses.intellij.utils.RequiredPluginsCheckerUtil.filterMissingOrDisabledPluginNames;
import static fi.aalto.cs.apluscourses.intellij.utils.RequiredPluginsCheckerUtil.filterMissingPluginDescriptors;
import static fi.aalto.cs.apluscourses.intellij.utils.RequiredPluginsCheckerUtil.getRequiredPlugins;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.EnablePluginsNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.InstallPluginsNotification;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * An action that checks and hints on missing or disabled required for the course plugins.
 */
public class RequiredPluginsCheckerAction extends DumbAwareAction {

  public static final String ACTION_ID = RequiredPluginsCheckerAction.class.getCanonicalName();

  /**
   * An actual work to filter out invalid (missing or disabled) plugins and notify gets done here.
   *
   * @param e {@link AnActionEvent}
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    List<IdeaPluginDescriptor> missingOrDisabledIdeaPluginDescriptors =
        getActualListOfMissingOrDisabledIdeaPluginDescriptors(e.getProject());
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
  private List<IdeaPluginDescriptor> getActualListOfMissingOrDisabledIdeaPluginDescriptors(
      Project project) {
    Map<String, String> requiredPlugins = getRequiredPlugins(project);
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
