package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CourseConfigurationErrorTest {

  @Test
  public void testCourseConfigurationError() {
    String errorMessage = "My test error message";
    Exception exception = new Exception(errorMessage);
    CourseConfigurationError notification = new CourseConfigurationError(exception);

    assertEquals("Group ID should be 'A+'",
        "A+", notification.getGroupId());
    assertEquals("Title should be 'A+ Courses failed to parse the course configuration file'",
        "Failed to parse the course configuration file",
        notification.getTitle());
    assertThat("Content should contain the message of the exception given to constructor.",
        notification.getContent(), containsString(errorMessage));
    assertSame("Exception should be same as the one that was given to the constructor",
        exception, notification.getException());
  }
}
