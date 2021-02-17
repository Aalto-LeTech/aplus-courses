package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class UrlRenderingErrorNotification extends Notification {

  @NotNull
  private final Exception exception;

  /**
   * Construct a notification informing the user that an error occurred while attempting to render
   * a submission.
   */
  public UrlRenderingErrorNotification(@NotNull Exception exception) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.UrlRenderingErrorNotification.title"),
        getAndReplaceText("notification.UrlRenderingErrorNotification.content",
            exception.getMessage()),
        NotificationType.ERROR
    );
    this.exception = exception;
  }

  @NotNull
  public Exception getException() {
    return exception;
  }

}
