package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public class MissingFileNotification extends Notification {

  /**
   * Construct a missing file notification that explains that a file with the given name couldn't be
   * found in the given module.
   */
  public MissingFileNotification(@NotNull String moduleName, @NotNull String filename) {
    super(
        "A+",
        "Could not find file",
        "A+ Courses plugin couldn't find the file " + filename + " in module " + moduleName
            + ". Please double-check from which module you intend to submit.",
        NotificationType.ERROR);
  }

}
