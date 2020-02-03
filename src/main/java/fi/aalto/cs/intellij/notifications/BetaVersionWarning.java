package fi.aalto.cs.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.intellij.common.Version;
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
    super("A+",
        "A+ Courses plugin is under development",
        "You are using version " + version + " of A+ Courses plugin, "
            + "which is a pre-release version of the plugin and still under development. \n "
            + "Some features of this plugin are still probably missing, "
            + "and the plugin is not yet tested thoroughly. "
            + "Use this plugin with caution and on your own risk!",
        NotificationType.WARNING);
    this.version = version;
  }

  @NotNull
  public Version getVersion() {
    return version;
  }
}
