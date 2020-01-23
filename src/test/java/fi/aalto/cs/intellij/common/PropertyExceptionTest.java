package fi.aalto.cs.intellij.common;

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
    assertSame(properties, exception.getProperties());
    assertEquals(propertyKey, exception.getPropertyKey());
    assertEquals(cause, exception.getCause());
    assertThat(exception.getMessage(), containsString("Property"));
    assertThat(exception.getMessage(), containsString(propertyKey));
    assertThat(exception.getMessage(), containsString(message));
  }
}
