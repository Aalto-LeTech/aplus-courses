package fi.aalto.cs.intellij.notifications;

import static fi.aalto.cs.intellij.utils.RequiredPluginsCheckerUtil.getPluginsNamesString;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import java.util.List;

/**
 * A {@link Notification} wrapper to let the user know about some required plugins disabled (with
 * names of them).
 */
public class EnablePluginsNotification extends Notification {

  /**
   * Builds the notification.
   */
  public EnablePluginsNotification(List<IdeaPluginDescriptor> disabledPlugins) {
    super("A+",
        "A+",
        "Some plugins must be and enabled for the A+ plugin to work properly ("
            + getPluginsNamesString(disabledPlugins) + ").",
        NotificationType.WARNING);
  }
}
