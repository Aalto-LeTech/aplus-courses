package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class MissingDependencyNotification extends Notification {
  /**
   * Construct a missing module notification which explains that modules with the given names couldn't be found.
   */
  public MissingDependencyNotification(@NotNull Set<String> moduleNames) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.MissingDependencyNotification.title"),
        getAndReplaceText("notification.MissingDependencyNotification.content", String.join(", ", moduleNames)),
        NotificationType.ERROR);
  }

}
