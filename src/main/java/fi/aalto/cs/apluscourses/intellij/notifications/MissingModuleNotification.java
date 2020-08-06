package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

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
    super("A+",
        getText("notification.MissingModuleNotification.title"),
        getAndReplaceText("notification.MissingModuleNotification.content", moduleName),
        NotificationType.ERROR);
    this.moduleName = moduleName;
  }

  @NotNull
  public String getModuleName() {
    return moduleName;
  }

}
