package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.Properties;
import org.junit.Test;

public class PropertyExceptionTest {

  @Test
  public void testCreatePropertyException() {
    Properties properties = new Properties();
    String propertyKey = "some-key";
    String message = "This is a cool message.";
    Throwable cause = new Throwable();

    PropertyException exception = new PropertyException(properties, propertyKey, message, cause);
    assertSame("Properties should be those given in the constructor.",
        properties, exception.getProperties());
    assertEquals("Property key should be the same that is given to the constructor.",
        propertyKey, exception.getPropertyKey());
    assertSame("Cause of exception should be the one given to the constructor.",
        cause, exception.getCause());
    assertThat("Message should contain the property key",
        exception.getMessage(), containsString(propertyKey));
    assertThat("Message should contain the message given to the constructor.",
        exception.getMessage(), containsString(message));
  }
}
