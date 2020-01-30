package fi.aalto.cs.intellij.notifications;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CourseConfigurationErrorTest {

  @Test
  public void testCourseConfigurationError() {
    String errorMessage = "My test error message";
    CourseConfigurationError notification = new CourseConfigurationError(errorMessage);

    assertEquals("Group ID should be 'A+'",
        "A+", notification.getGroupId());
    assertEquals("Title should be 'A+ Courses failed to parse the course configuration file'",
        "A+ Courses plugin failed to parse the course configuration file",
        notification.getTitle());
    assertThat("Content should contain the error message given to the constructor.",
        notification.getContent(), containsString(errorMessage));
  }

}
