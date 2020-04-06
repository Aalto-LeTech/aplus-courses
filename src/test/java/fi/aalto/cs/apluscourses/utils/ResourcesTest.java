package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ResourcesTest {

  @Test
  public void testGetProperties() throws ResourceException {
    AtomicInteger closeCallCounter = new AtomicInteger(0);

    Resources res = new Resources(name -> {
      assertEquals("correct-properties", name);

      return new ByteArrayInputStream("a=x\nb=y\n".getBytes()) {

        @Override
        public void close() throws IOException {
          closeCallCounter.getAndIncrement();
          super.close();
        }
      };
    });

    assertEquals("Stream should not be closed by the constructor.",
        0, closeCallCounter.get());

    Properties props = res.getProperties("correct-properties");

    assertEquals("Stream should be closed by the getProperties().",
        1, closeCallCounter.get());
    assertEquals("getProperty(\"a\") should return the value set for 'a'",
        "x", props.getProperty("a"));
    assertEquals("getProperty(\"b\") should return the value set for 'b'",
        "y", props.getProperty("b"));
  }

  @Test
  public void testGetPropertiesWithError() {
    IOException exception = new IOException();

    Resources res = new Resources(name -> new InputStream() {
      @Override
      public int read() throws IOException {
        throw exception;
      }
    });
    String resourceName = "inaccessible-resource";

    try {
      res.getProperties(resourceName);
    } catch (ResourceException ex) {
      assertEquals("Exception should have the requested resource name.",
          resourceName, ex.getResourceName());
      assertSame("The cause of the expression should come from the stream.",
          exception, ex.getCause());
      return;
    }
    fail("getProperties() should throw an exception if the stream cannot be read.");
  }

  @Test
  public void testGetPropertiesWithNullStream() {
    Resources res = new Resources(name -> null);
    String resourceName = "null-stream-resource";

    try {
      res.getProperties(resourceName);
    } catch (ResourceException ex) {
      assertEquals("Exception should have the requested resource name.",
          resourceName, ex.getResourceName());
      return;
    }
    fail("getProperties() should throw an exception if the stream is null");
  }

  @Test
  public void testGetIcon() throws ResourceException {

  }

  @Test
  public void testGetIconWithError() {

  }

  @Test
  public void testGetIconWithNullStream() {
    Resources res = new Resources(name -> null);
    String resourceName = "null-stream-resource";

    try {
      res.getIcon(resourceName);
    } catch (ResourceException ex) {
      assertEquals("Exception should have the requested resource name.",
          resourceName, ex.getResourceName());
      return;
    }
    fail("getIcon() should throw an exception if the stream is null");
  }
}
