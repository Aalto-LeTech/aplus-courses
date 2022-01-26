package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PropertyReaderTest {

  @Test
  void testGetProperty() throws PropertyException {
    Properties properties = new Properties();
    properties.setProperty("a", "x");
    properties.setProperty("b", "y");

    PropertyReader reader = new PropertyReader(properties);
    Assertions.assertEquals("x", reader.getProperty("a"), "getProperty(\"x\") should return the value set for 'x'");
    Assertions.assertEquals("y", reader.getProperty("b"), "getProperty(\"y\") should return the value set for 'y'");
  }

  @Test
  void testGetPropertyWhichDoesNotExist() {
    String propertyKey = "nonExistentProperty";

    PropertyReader reader = new PropertyReader(new Properties());
    try {
      reader.getProperty(propertyKey);
    } catch (PropertyException ex) {
      Assertions.assertEquals(propertyKey, ex.getPropertyKey(),
          "Exception should have the property key that was requested.");
      return;
    }
    Assertions.fail("getProperty() should throw a PropertyException if the key is not there.");
  }

  @Test
  void testGetPropertyAsObject() throws PropertyException {
    String propertyKey = "foo";
    String propertyValue = "bar";
    Object obj = new Object();

    Properties properties = new Properties();
    properties.setProperty(propertyKey, propertyValue);

    PropertyReader reader = new PropertyReader(properties);
    Object result = reader.getPropertyAsObject(propertyKey, value -> {
      Assertions.assertEquals(propertyValue, value,
          "Parser should be provided with the value corresponding the requested key.");
      return obj;
    });

    Assertions.assertSame(obj, result, "getPropertyAsObject() should return the object provided by the parser");
  }

  @Test
  void testGetPropertyAsObjectWithError() {
    String propertyKey = "someKey";
    RuntimeException exception = new UnsupportedOperationException();
    PropertyReader.ValueParser<Object> parser = value -> {
      throw exception;
    };

    Properties properties = new Properties();
    properties.setProperty(propertyKey, "whatever");

    PropertyReader reader = new PropertyReader(properties);
    try {
      reader.getPropertyAsObject(propertyKey, parser);
    } catch (PropertyException ex) {
      Assertions.assertEquals(propertyKey, ex.getPropertyKey(), "Exception should have the requested key.");
      Assertions.assertSame(exception, ex.getCause(), "The cause of the exception should come from the parser.");
      return;
    }
    Assertions.fail("getPropertyAsObject() should throw a PropertyException if the key is not there.");
  }
}
