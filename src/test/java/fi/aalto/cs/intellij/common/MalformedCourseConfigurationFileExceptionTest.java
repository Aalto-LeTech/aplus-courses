package fi.aalto.cs.intellij.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MalformedCourseConfigurationFileExceptionTest {

  @Test
  public void testCreateMalformedCourseConfigurationFileException() {
    Throwable cause = new Throwable();
    String path = "./path/to/course/configuration/file";
    MalformedCourseConfigurationFileException exception =
        new MalformedCourseConfigurationFileException(path, "Awesome message", cause);
    assertEquals("The cause of the exception should be the one given to the constructor",
        cause, exception.getCause());
    assertEquals("The configuration file path should be the one given to the constructor",
        path, exception.getPathToConfigurationFile());
  }

}
