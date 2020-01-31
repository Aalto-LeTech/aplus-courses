package fi.aalto.cs.intellij.common;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

public class NoSuchModuleExceptionTest {

  @Test
  public void testCreateNoSuchModuleException() {
    Throwable cause = new Throwable();
    Course course = new Course("Course Name", Collections.emptyList(), Collections.emptyMap());
    NoSuchModuleException exception = new NoSuchModuleException(course, "Awesome message", cause);
    assertEquals("The cause of the exception should be the one given to the constructor",
        cause, exception.getCause());
    assertEquals("The course should be the one given to the constructor", course,
        exception.getCourse());
  }

}
