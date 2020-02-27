package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.intellij.utils.RequiredPluginsCheckerUtil.getPluginsNamesString;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import java.util.List;

/**
 * A {@link Notification} wrapper to let the user know about some required plugins missing (with
 * names of them).
 */
public class InstallPluginsNotification extends Notification {

  /**
   * Builds the notification.
   *
   * @param missingPluginDescriptors is a {@link List} of {@link IdeaPluginDescriptor} that are
   *                                 missing.
   */
  public InstallPluginsNotification(List<IdeaPluginDescriptor> missingPluginDescriptors) {
    super("A+",
        "A+ Courses plugin required plugins missing warning",
        "The additional plugin(s) must be installed and enabled for the A+ plugin to work "
            + "properly (" + getPluginsNamesString(missingPluginDescriptors) + ").",
        NotificationType.WARNING);
  }
}
