package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceExceptionTest {

  @Test
  void testCreateResourceException() {
    String resourceName = "some-resource";
    String message = "This is a nice message.";
    Throwable cause = new Throwable();

    ResourceException exception = new ResourceException(resourceName, message, cause);
    Assertions.assertEquals(resourceName, exception.getResourceName(),
        "Resource name should be the same as that given to the constructor.");
    Assertions.assertSame(cause, exception.getCause(),
        "Cause of the exception should be the one given to the constructor.");
    MatcherAssert.assertThat("Message should contain the resource name.", exception.getMessage(),
        containsString(resourceName));
    MatcherAssert.assertThat("Message should contain the message given to the constructor", exception.getMessage(),
        containsString(message));
  }
}
