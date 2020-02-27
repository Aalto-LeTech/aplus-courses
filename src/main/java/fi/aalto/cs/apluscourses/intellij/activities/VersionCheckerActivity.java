package fi.aalto.cs.apluscourses.intellij.activities;

import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.apluscourses.intellij.notifications.BetaVersionWarning;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.Version;
import org.jetbrains.annotations.NotNull;

/**
 * An activity that checks, on startup, if the version of the plugin is a pre-release, and, if that
 * is the case, shows a {@link BetaVersionWarning}.
 */
public class VersionCheckerActivity implements StartupActivity {

  @NotNull
  private final Notifier notifier;
  @NotNull
  private final Version version;

  VersionCheckerActivity(@NotNull Version version, @NotNull Notifier notifier) {
    this.version = version;
    this.notifier = notifier;
  }

  /**
   * Instantiates a new {@link VersionCheckerActivity}.
   * This constructor is called by the IntelliJ framework.
   */
  public VersionCheckerActivity() {
    this(BuildInfo.INSTANCE.version, Notifications.Bus::notify);
  }

  /**
   * This method is called by the IntelliJ framework.
   * @param project The current project.
   */
  @Override
  public void runActivity(@NotNull Project project) {
    if (version.major < 1) {
      notifier.notify(new BetaVersionWarning(version), project);
    }
  }
}
