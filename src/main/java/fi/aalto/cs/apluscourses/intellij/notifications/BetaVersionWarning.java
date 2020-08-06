package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.utils.Version;
import org.jetbrains.annotations.NotNull;

public class BetaVersionWarning extends Notification {
  @NotNull
  private final Version version;

  /**
   * Constructs a notification that warns a user that they are using a pre-release version of the
   * software.
   * @param version Version of the software.
   */
  public BetaVersionWarning(@NotNull Version version) {
    super(
        getText("general.aPlus"),
        getText("notification.BetaVersionWarning.title"),
        getAndReplaceText("notification.BetaVersionWarning.content", version),
        NotificationType.WARNING);
    this.version = version;
  }

  @NotNull
  public Version getVersion() {
    return version;
  }
}
