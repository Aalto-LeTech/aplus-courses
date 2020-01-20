package fi.aalto.cs.intellij.activities;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.intellij.common.BuildInfo;
import fi.aalto.cs.intellij.notifications.APlusNotifications;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class VersionCheckerActivityTest {

  @Test
  public void testRunActivityWithPreReleaseVersion() {
    int[] numberOfCalls = { 0 };

    StartupActivity activity = new VersionCheckerActivity(
        new BuildInfo.Version(0, 22, 315),
        (notification, project) -> {
          numberOfCalls[0]++;
          Assert.assertNull(project);
          Assert.assertThat(notification,
              CoreMatchers.instanceOf(APlusNotifications.BetaVersionWarning.class));
          Assert.assertEquals("0.22.315",
              ((APlusNotifications.BetaVersionWarning) notification).getVersion().toString());
        });

    activity.runActivity(Mockito.mock(Project.class));

    Assert.assertEquals(1, numberOfCalls[0]);
  }

  @Test
  public void testRunActivityWithPostReleaseVersion() {
    StartupActivity activity = new VersionCheckerActivity(
        new BuildInfo.Version(1, 24, 228),
        (notification, project) -> {
          Assert.fail();
        });

    activity.runActivity(Mockito.mock(Project.class));
  }
}
