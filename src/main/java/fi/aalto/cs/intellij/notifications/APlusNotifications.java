package fi.aalto.cs.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.intellij.common.BuildInfo;
import org.jetbrains.annotations.NotNull;

/**
 * A container class for notifications used by A+ plugin.
 */
public class APlusNotifications {

  public static class BetaVersionWarning extends Notification {
    private final BuildInfo.Version version;

    /**
     * Constructs a notification that warns a user that they are using a pre-release version of the
     * software.
     * @param version Version of the software.
     */
    public BetaVersionWarning(@NotNull BuildInfo.Version version) {
      super("A+",
          "A+ Courses plugin is under development",
          "You are using version " + version + " of A+ Courses plugin, "
              + "which is a pre-release version of the plugin and still under development. "
              + "Some features of this plugin are still probably missing, "
              + "and the plugin is not yet tested thoroughly. "
              + "Use this plugin with caution and on your own risk!",
          NotificationType.WARNING);
      this.version = version;
    }

    @NotNull
    public BuildInfo.Version getVersion() {
      return version;
    }
  }
}
