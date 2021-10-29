package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class NoSuchComponentExceptionTest {

  @Test
  public void testCreateNoSuchComponentException() {
    Throwable cause = new Throwable();
    String componentName = "Awesome component";
    NoSuchComponentException exception = new NoSuchComponentException(componentName, cause);
    assertEquals("The cause of the exception should be the one given to the constructor",
        cause, exception.getCause());
    assertThat("The message should contain the name of the module",
        exception.getMessage(), containsString(componentName));
  }
}
