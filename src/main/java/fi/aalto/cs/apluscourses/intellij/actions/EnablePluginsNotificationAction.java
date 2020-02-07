package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.RequiredPluginsCheckerUtil.getPluginsNamesString;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper on {@link NotificationAction} to interactively <b>enable</b> provided plugins.
 */
public class EnablePluginsNotificationAction extends NotificationAction {

  private List<IdeaPluginDescriptor> disabledPluginDescriptors;
  private final PluginProvider pluginProvider;

  /**
   * Builds the action.
   *
   * @param disabledPluginDescriptors is a {@link List} of {@link IdeaPluginDescriptor} that can be
   *                                  enabled.
   */
  public EnablePluginsNotificationAction(List<IdeaPluginDescriptor> disabledPluginDescriptors) {
    this(disabledPluginDescriptors, PluginManager::getPlugin);
  }

  /**
   * Builds the action.
   *
   * @param disabledPluginDescriptors is a {@link List} of {@link IdeaPluginDescriptor} that can be
   *                                  enabled.
   * @param pluginProvider            is a {@link PluginProvider} exposed for testing purposes.
   */
  public EnablePluginsNotificationAction(
      @NotNull List<IdeaPluginDescriptor> disabledPluginDescriptors,
      PluginProvider pluginProvider) {
    super("Enable the required plugin(s) ("
        + getPluginsNamesString(disabledPluginDescriptors) + ").");
    this.disabledPluginDescriptors = disabledPluginDescriptors;
    this.pluginProvider = pluginProvider;
  }

  /**
   * Activate all the required plugins.
   *
   * @param e            an {@link AnActionEvent}.
   * @param notification a {@link Notification} to handle.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    disabledPluginDescriptors.forEach(descriptor -> Objects
        .requireNonNull(pluginProvider.getPlugin(descriptor.getPluginId())).setEnabled(true));
    notification.expire();
  }

  /**
   * An abstract interface for an object that provides {@link IdeaPluginDescriptor} based on
   * supplied {@link PluginId}s. The most useful realization of this interface is {@code
   * PluginManager::getPlugin}.
   */
  @FunctionalInterface
  public interface PluginProvider {

    /**
     * Searches for an {@link IdeaPluginDescriptor} with the {@code pluginId}.
     *
     * @param pluginId is the id of the desired plugin.
     * @return an {@link IdeaPluginDescriptor} which corresponds to {@code pluginId} or {@code
     * null} if the plugin could not be found.
     */
    IdeaPluginDescriptor getPlugin(PluginId pluginId);
  }
}
