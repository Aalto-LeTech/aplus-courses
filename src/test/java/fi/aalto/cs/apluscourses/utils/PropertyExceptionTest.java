package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.Properties;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PropertyExceptionTest {

  @Test
  void testCreatePropertyException() {
    Properties properties = new Properties();
    String propertyKey = "some-key";
    String message = "This is a cool message.";
    Throwable cause = new Throwable();

    PropertyException exception = new PropertyException(properties, propertyKey, message, cause);
    Assertions.assertSame(properties, exception.getProperties(),
        "Properties should be those given in the constructor.");
    Assertions.assertEquals(propertyKey, exception.getPropertyKey(),
        "Property key should be the same that is given to the constructor.");
    Assertions.assertSame(cause, exception.getCause(),
        "Cause of exception should be the one given to the constructor.");
    MatcherAssert.assertThat("Message should contain the property key", exception.getMessage(),
        containsString(propertyKey));
    MatcherAssert.assertThat("Message should contain the message given to the constructor.", exception.getMessage(),
        containsString(message));
  }
}
