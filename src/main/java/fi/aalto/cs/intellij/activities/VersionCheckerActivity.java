package fi.aalto.cs.intellij.activities;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.intellij.common.BuildInfo;
import fi.aalto.cs.intellij.notifications.APlusNotifications;
import fi.aalto.cs.intellij.notifications.Notifier;
import org.jetbrains.annotations.NotNull;

/**
 * An activity that checks, on startup, if the version of the plugin is a pre-release, and, if that
 * is the case, shows a {@link APlusNotifications.BetaVersionWarning}.
 */
public class VersionCheckerActivity implements StartupActivity {

  private final Notifier notifier;
  private final BuildInfo.Version version;

  VersionCheckerActivity(@NotNull BuildInfo.Version version,
                         @NotNull Notifier notifier) {
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

    Notification notification = new APlusNotifications.BetaVersionWarning(version);

    notifier.notify(notification, null);
  }
}
