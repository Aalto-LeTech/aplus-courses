package fi.aalto.cs.intellij.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import fi.aalto.cs.intellij.model.Course;
import fi.aalto.cs.intellij.model.NoSuchModuleException;
import java.util.Collections;

import org.junit.Test;

public class NoSuchModuleExceptionTest {

  @Test
  public void testCreateNoSuchModuleException() {
    Throwable cause = new Throwable();
    String moduleName = "Awesome module";
    Course course = new Course("Course Name", Collections.emptyList(), Collections.emptyMap());
    NoSuchModuleException exception = new NoSuchModuleException(course, moduleName, cause);
    assertEquals("The cause of the exception should be the one given to the constructor",
        cause, exception.getCause());
    assertEquals("The course should be the one given to the constructor", course,
        exception.getCourse());
    assertThat("The message should contain the name of the module",
        exception.getMessage(), containsString(moduleName));
  }

}
