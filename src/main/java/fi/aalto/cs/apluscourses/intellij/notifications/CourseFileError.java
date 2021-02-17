package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class CourseFileError extends Notification {

  /**
   * Error to be shown when course settings file cannot be accessed.
   *
   * @param e Exception.
   */
  public CourseFileError(Exception e) {
    super(PluginSettings.A_PLUS,
        getText("notification.CourseFileError.title"),
        getAndReplaceText("notification.CourseFileError.content", e.getMessage()),
        NotificationType.ERROR);
  }
}
