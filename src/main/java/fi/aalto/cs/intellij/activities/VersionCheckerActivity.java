package fi.aalto.cs.intellij.activities;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.intellij.common.BuildInfo;
import fi.aalto.cs.intellij.common.Notifier;
import org.jetbrains.annotations.NotNull;

public class VersionCheckerActivity implements StartupActivity {

  private final Notifier notifier;
  private final BuildInfo.Version version;

  public VersionCheckerActivity(@NotNull BuildInfo.Version version, @NotNull Notifier notifier) {
    this.version = version;
    this.notifier = notifier;
  }

  public VersionCheckerActivity() {
    this(BuildInfo.SingletonUtility.INSTANCE.version, Notifications.Bus::notify);
  }

  @Override
  public void runActivity(@NotNull Project project) {
    if (version.major >= 1) {
      return;
    }

    Notification notification = new Notification(
        "A+",
        "A+ Courses plugin is under development",
        "You are using version " + version + " of A+ Courses plugin, "
        + "which is a pre-release version of the plugin and still under development. "
        + "Some features of this plugin are still probably missing, "
        + "and the plugin is not yet tested thoroughly. "
        + "Use this plugin with caution and on your own risk!",
        NotificationType.WARNING);

    notifier.notify(notification, null);
  }
}
