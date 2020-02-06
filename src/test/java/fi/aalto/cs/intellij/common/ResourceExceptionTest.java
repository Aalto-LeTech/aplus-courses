package fi.aalto.cs.intellij.common;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ResourceExceptionTest {

  @Test
  public void testCreateResourceException() {
    String resourceName = "some-resource";
    String message = "This is a nice message.";
    Throwable cause = new Throwable();

    ResourceException exception = new ResourceException(resourceName, message, cause);
    assertEquals("Resource name should be the same as that given to the constructor.",
        resourceName, exception.getResourceName());
    assertSame("Cause of the exception should be the one given to the constructor.",
        cause, exception.getCause());
    assertThat("Message should contain the resource name.",
        exception.getMessage(), containsString(resourceName));
    assertThat("Message should contain the message given to the constructor",
        exception.getMessage(), containsString(message));
  }
}
