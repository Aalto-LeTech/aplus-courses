package fi.aalto.cs.intellij.actions;

import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.getPluginsNamesString;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper on {@link NotificationAction} to interactively <b>enable</b> provided plugins.
 */
public class EnablePluginsNotificationAction extends NotificationAction {

  private List<IdeaPluginDescriptor> disabledPluginDescriptors;
  private final PluginEnabler pluginEnabler;

  /**
   * Builds the action.
   *
   * @param disabledPluginDescriptors is a {@link List} of {@link IdeaPluginDescriptor} that can be
   *                                  enabled.
   */
  public EnablePluginsNotificationAction(List<IdeaPluginDescriptor> disabledPluginDescriptors) {
    this(disabledPluginDescriptors, PluginManager::enablePlugin);
  }

  /**
   * Builds the action.
   *
   * @param disabledPluginDescriptors is a {@link List} of {@link IdeaPluginDescriptor} that can be
   *                                  enabled.
   * @param pluginEnabler             is a {@link PluginEnabler} exposed for testing purposes.
   */
  public EnablePluginsNotificationAction(
      @NotNull List<IdeaPluginDescriptor> disabledPluginDescriptors,
      PluginEnabler pluginEnabler) {
    super("Enable the required plugin(s) ("
        + getPluginsNamesString(disabledPluginDescriptors) + ").");
    this.disabledPluginDescriptors = disabledPluginDescriptors;
    this.pluginEnabler = pluginEnabler;
  }

  /**
   * Activate all the required plugins.
   *
   * @param e            an {@link AnActionEvent}.
   * @param notification a {@link Notification} to handle.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    disabledPluginDescriptors
        .forEach(descriptor -> pluginEnabler.enable(descriptor.getPluginId().getIdString()));
    notification.expire();
  }

  /**
   * An abstract interface for an object that enables plugins based on supplied {@link String} that
   * represent {@link PluginId}s. The most useful realization of this interface is {@code
   * PluginManager::enablePlugin}.
   */
  @FunctionalInterface
  public interface PluginEnabler {

    /**
     * Enables a plugin based on the given PluginId represented as {@link String}.
     *
     * @param pluginId is the {@link String} id of the desired plugin.
     * @return is true for the case of successful enablement and false if it did not work.
     */
    boolean enable(String pluginId);
  }
}
