package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.CoreMatchers.containsString;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseConfigurationErrorTest {

  @Test
  void testCourseConfigurationError() {
    String errorMessage = "My test error message";
    Exception exception = new Exception(errorMessage);
    CourseConfigurationError notification = new CourseConfigurationError(exception);

    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be 'A+'");
    Assertions.assertEquals("Failed to parse the course configuration file", notification.getTitle(),
        "Title should be 'A+ Courses failed to parse the course configuration file'");
    MatcherAssert.assertThat("Content should contain the message of the exception given to constructor.",
        notification.getContent(), containsString(errorMessage));
    Assertions.assertSame(exception, notification.getException(),
        "Exception should be same as the one that was given to the constructor");
  }
}
