package fi.aalto.cs.intellij.actions;

import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.getPluginsNamesString;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper on {@link NotificationAction} to interactively <b>enable</b> provided plugins.
 */
public class EnablePluginsNotificationAction extends NotificationAction {

  private List<IdeaPluginDescriptor> disabledPluginDescriptors;

  /**
   * Builds the action.
   */
  public EnablePluginsNotificationAction(List<IdeaPluginDescriptor> disabledPluginDescriptors) {
    super("Enable the required plugin(s) ("
        + getPluginsNamesString(disabledPluginDescriptors) + ").");
    this.disabledPluginDescriptors = disabledPluginDescriptors;
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
        .requireNonNull(PluginManager.getPlugin(descriptor.getPluginId())).setEnabled(true));
    notification.expire();
  }
}
