package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CoursePluginVersionTest {

  @Test
  public void testCheckVersion() {
    CoursePluginVersion highestVersion = new CoursePluginVersion(Integer.MAX_VALUE, 0);
    CoursePluginVersion lowestVersion = new CoursePluginVersion(Integer.MIN_VALUE, 0);

    assertEquals("Update is correctly marked as mandatory",
        CoursePluginVersion.Status.UPDATE_REQUIRED, highestVersion.checkVersion());

    assertEquals("Version is correctly recognized as up-to-date",
        CoursePluginVersion.Status.VALID, lowestVersion.checkVersion());
  }
}
