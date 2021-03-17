package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CoursePluginVersionTest {

  @Test
  public void testCheckVersion() {
    CoursePluginVersion highestVersion = new CoursePluginVersion(Integer.MAX_VALUE, 0, null);
    CoursePluginVersion lowestVersion = new CoursePluginVersion(Integer.MIN_VALUE, 0, null);

    assertEquals("Update is correctly marked as mandatory",
        highestVersion.checkVersion(), CoursePluginVersion.Status.UPDATE_REQUIRED);

    assertEquals("Version is correctly recognized as up-to-date",
        lowestVersion.checkVersion(), CoursePluginVersion.Status.VALID);
  }

  @Test
  public void testGetPrettyVersion() {
    CoursePluginVersion version = new CoursePluginVersion(0, 0, "v37.11");

    assertEquals("Required version has the correct plugin version string",
        version.getPrettyVersion(), "v37.11");
  }
}
