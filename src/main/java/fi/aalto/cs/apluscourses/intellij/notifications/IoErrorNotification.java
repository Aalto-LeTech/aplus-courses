package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class IoErrorNotification extends Notification {

  @NotNull
  private final IOException exception;

  /**
   * Construct an error notification that tells the user of an IO error.
   *
   * @param exception The exception corresponding to the IO error.
   */
  public IoErrorNotification(@NotNull IOException exception) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.IoErrorNotification.title"),
        getAndReplaceText("notification.IoErrorNotification.content",
            exception.getMessage()),
        NotificationType.ERROR);
    this.exception = exception;
  }

  @NotNull
  public IOException getException() {
    return exception;
  }

}
