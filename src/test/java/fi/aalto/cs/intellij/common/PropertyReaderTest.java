package fi.aalto.cs.intellij.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Properties;
import org.junit.Test;

public class PropertyReaderTest {

  @Test
  public void testGetProperty() throws PropertyException {
    Properties properties = new Properties();
    properties.setProperty("a", "x");
    properties.setProperty("b", "y");

    PropertyReader reader = new PropertyReader(properties);
    assertEquals("x", reader.getProperty("a"));
    assertEquals("y", reader.getProperty("b"));
  }

  @Test
  public void testGetPropertyWhichDoesNotExist() {
    String propertyKey = "nonExistentProperty";

    PropertyReader reader = new PropertyReader(new Properties());
    try {
      reader.getProperty(propertyKey);
    } catch (PropertyException ex) {
      assertEquals(propertyKey, ex.getPropertyKey());
      return;
    }
    fail();
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
      assertEquals(propertyValue, value);
      return obj;
    });

    assertSame(obj, result);
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
      assertEquals(propertyKey, ex.getPropertyKey());
      assertSame(exception, ex.getCause());
      return;
    }
    fail();
  }
}
