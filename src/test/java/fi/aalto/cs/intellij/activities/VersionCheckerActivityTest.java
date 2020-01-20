package fi.aalto.cs.intellij.activities;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;

import fi.aalto.cs.intellij.common.BuildInfo;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class VersionCheckerActivityTest {

  @Test
  public void test_runActivity_preReleaseVersion() {
    int[] numberOfCalls = { 0 };

    StartupActivity activity = new VersionCheckerActivity(
        new BuildInfo.Version(0, 22, 315),
        (notification, project) -> {
          numberOfCalls[0]++;
          Assert.assertNull(project);
          Assert.assertEquals("A+", notification.getGroupId());
          Assert.assertEquals("A+ Courses plugin is under development", notification.getTitle());
          Assert.assertThat(notification.getContent(), CoreMatchers.containsString("0.22.315"));
        });

    activity.runActivity(Mockito.mock(Project.class));

    Assert.assertEquals(1, numberOfCalls[0]);
  }

  @Test
  public void test_runActivity_postReleaseVersion() {
    StartupActivity activity = new VersionCheckerActivity(
        new BuildInfo.Version(1, 24, 228),
        (notification, project) -> {
          Assert.fail();
        });

    activity.runActivity(Mockito.mock(Project.class));
  }
}
