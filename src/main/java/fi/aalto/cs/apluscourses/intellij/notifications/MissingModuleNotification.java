package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class MissingModuleNotification extends Notification {

  @NotNull
  private final String moduleName;

  /**
   * Construct a missing module notification that explains that a module with the given name
   * couldn't be found.
   */
  public MissingModuleNotification(@NotNull String moduleName) {
    super(
        PluginSettings.A_PLUS,
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
