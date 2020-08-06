package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public class SubmissionRenderingErrorNotification extends Notification {

  @NotNull
  private final Exception exception;

  /**
   * Construct a notification informing the user that an error occurred while attempting to render
   * a submission.
   */
  public SubmissionRenderingErrorNotification(@NotNull Exception exception) {
    super(
        getText("general.aPlus"),
        getText("notification.SubmissionRenderingErrorNotification.title"),
        getAndReplaceText("notification.SubmissionRenderingErrorNotification.content",
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
