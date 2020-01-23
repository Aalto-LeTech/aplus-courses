package fi.aalto.cs.intellij.activities;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.intellij.common.Version;
import fi.aalto.cs.intellij.notifications.BetaVersionWarning;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class VersionCheckerActivityTest {

  @Test
  public void testRunActivityWithPreReleaseVersion() {
    AtomicInteger numberOfCalls = new AtomicInteger(0);
    Project someProject = mock(Project.class);

    StartupActivity activity = new VersionCheckerActivity(
        new Version(0, 22, 315),
        (notification, project) -> {
          numberOfCalls.getAndIncrement();
          assertSame(someProject, project);
          assertThat(notification, instanceOf(BetaVersionWarning.class));
          assertEquals("0.22.315",
              ((BetaVersionWarning) notification).getVersion().toString());
        });

    activity.runActivity(someProject);

    assertEquals(1, numberOfCalls.get());
  }

  @Test
  public void testRunActivityWithPostReleaseVersion() {
    StartupActivity activity = new VersionCheckerActivity(
        new Version(1, 24, 228),
        (notification, project) -> fail());

    activity.runActivity(mock(Project.class));
  }
}
