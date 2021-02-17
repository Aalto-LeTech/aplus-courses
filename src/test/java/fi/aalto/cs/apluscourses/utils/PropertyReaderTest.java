package fi.aalto.cs.apluscourses.utils;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class PropertyReaderTest {

  @Test
  public void testGetProperty() throws PropertyException {
    Properties properties = new Properties();
    properties.setProperty("a", "x");
    properties.setProperty("b", "y");

    PropertyReader reader = new PropertyReader(properties);
    assertEquals("getProperty(\"x\") should return the value set for 'x'",
        "x", reader.getProperty("a"));
    assertEquals("getProperty(\"y\") should return the value set for 'y'",
        "y", reader.getProperty("b"));
  }

  @Test
  public void testGetPropertyWhichDoesNotExist() {
    String propertyKey = "nonExistentProperty";

    PropertyReader reader = new PropertyReader(new Properties());
    try {
      reader.getProperty(propertyKey);
    } catch (PropertyException ex) {
      assertEquals("Exception should have the property key that was requested.",
          propertyKey, ex.getPropertyKey());
      return;
    }
    fail("getProperty() should throw a PropertyException if the key is not there.");
  }

  @Test
  public void testGetPropertyAsObject() throws PropertyException {
    String propertyKey = "foo";
    String propertyValue = "bar";
    Object obj = new Object();

    Properties properties = new Properties();
    properties.setProperty(propertyKey, propertyValue);

    PropertyReader reader = new PropertyReader(properties);
    Object result = reader.getPropertyAsObject(propertyKey, value -> {
      assertEquals("Parser should be provided with the value corresponding the requested key.",
          propertyValue, value);
      return obj;
    });

    assertSame("getPropertyAsObject() should return the object provided by the parser",
        obj, result);
  }

  @Test
  public void testGetPropertyAsObjectWithError() {
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
      assertEquals("Exception should have the requested key.",
          propertyKey, ex.getPropertyKey());
      assertSame("The cause of the exception should come from the parser.",
          exception, ex.getCause());
      return;
    }
    fail("getPropertyAsObject() should throw a PropertyException if the key is not there.");
  }
}
