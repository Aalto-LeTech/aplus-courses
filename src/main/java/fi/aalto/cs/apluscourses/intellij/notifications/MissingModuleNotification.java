package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public class MissingModuleNotification extends Notification {

  @NotNull
  private final String moduleName;

  /**
   * Construct a missing module notification that explains that a module with the given name
   * couldn't be found.
   */
  public MissingModuleNotification(@NotNull String moduleName) {
    super(
        "A+",
        "Could not find file",
        "A+ Courses plugin couldn't find the module " + moduleName + ".",
        NotificationType.ERROR);
    this.moduleName = moduleName;
  }

  @NotNull
  public String getModuleName() {
    return moduleName;
  }

}
