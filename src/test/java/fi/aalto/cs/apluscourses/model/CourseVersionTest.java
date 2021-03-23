package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CourseVersionTest {

  @Test
  public void testCheckVersion() {
    CourseVersion highestVersion = new CourseVersion(Integer.MAX_VALUE, 0);
    CourseVersion lowestVersion = new CourseVersion(Integer.MIN_VALUE, 0);

    assertEquals("Update is correctly marked as mandatory",
        CourseVersion.Status.UPDATE_REQUIRED, highestVersion.checkVersion());

    assertEquals("Version is correctly recognized as up-to-date",
        CourseVersion.Status.VALID, lowestVersion.checkVersion());
  }
}
