package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MalformedCourseConfigurationExceptionTest {

  @Test
  public void testCreateMalformedCourseConfigurationException() {
    Throwable cause = new Throwable();
    String path = "./path/to/course/configuration/file";
    MalformedCourseConfigurationException exception =
        new MalformedCourseConfigurationException(path, "Awesome message", cause);
    assertEquals("The cause of the exception should be the one given to the constructor",
        cause, exception.getCause());
    assertEquals("The configuration file path should be the one given to the constructor",
        path, exception.getPathToConfigurationFile());
  }
}
