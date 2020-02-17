package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class NoSuchModuleExceptionTest {

  @Test
  public void testCreateNoSuchModuleException() {
    Throwable cause = new Throwable();
    String moduleName = "Awesome module";
    NoSuchModuleException exception = new NoSuchModuleException(moduleName, cause);
    assertEquals("The cause of the exception should be the one given to the constructor",
        cause, exception.getCause());
    assertThat("The message should contain the name of the module",
        exception.getMessage(), containsString(moduleName));
  }

}
