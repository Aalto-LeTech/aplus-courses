package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class NetworkErrorNotification extends Notification {

  @NotNull
  private final Exception exception;

  /**
   * Constructs a notification that notifies the user of an IO error arising from the HTTP client.
   * @param exception An exception that caused this notification.
   */
  public NetworkErrorNotification(@NotNull Exception exception) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.NetworkErrorNotification.title"),
        getAndReplaceText("notification.NetworkErrorNotification.content",
            exception.getMessage()),
        NotificationType.ERROR);
    this.exception = exception;
  }

  @NotNull
  public Exception getException() {
    return exception;
  }
}
