package fi.aalto.cs.intellij.common;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ResourceExceptionTest {

  @Test
  public void testCreateResourceException() {
    String resourceName = "some-resource";
    String message = "This is a nice message.";
    Throwable cause = new Throwable();

    ResourceException exception = new ResourceException(resourceName, message, cause);
    assertEquals(resourceName, exception.getResourceName());
    assertEquals(cause, exception.getCause());
    assertThat(exception.getMessage(), containsString("Resource"));
    assertThat(exception.getMessage(), containsString(resourceName));
    assertThat(exception.getMessage(), containsString(message));
  }
}
