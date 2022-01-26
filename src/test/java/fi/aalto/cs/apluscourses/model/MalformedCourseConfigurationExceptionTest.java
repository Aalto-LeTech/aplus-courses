package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MalformedCourseConfigurationExceptionTest {

  @Test
  void testCreateMalformedCourseConfigurationException() {
    Throwable cause = new Throwable();
    String path = "./path/to/course/configuration/file";
    MalformedCourseConfigurationException exception =
        new MalformedCourseConfigurationException(path, "Awesome message", cause);
    Assertions.assertEquals(cause, exception.getCause(),
        "The cause of the exception should be the one given to the constructor");
    Assertions.assertEquals(path, exception.getPathToConfigurationFile(),
        "The configuration file path should be the one given to the constructor");
  }
}
