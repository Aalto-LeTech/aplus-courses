package fi.aalto.cs.intellij.common;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ResourcesTest {

  @Test
  public void testGetProperties() throws Resources.ResourceException {
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

    assertEquals(0, closeCallCounter.get());

    Properties props = res.getProperties("correct-properties");

    assertEquals(1, closeCallCounter.get());
    assertEquals("x", props.getProperty("a"));
    assertEquals("y", props.getProperty("b"));
  }

  @Test
  public void testGetPropertiesWithError() {
    Resources res = new Resources(name -> new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException();
      }
    });
    String resourceName = "inaccessible-resource";

    try {
      res.getProperties(resourceName);
    } catch (Resources.ResourceException ex) {
      assertThat(ex.getCause(), instanceOf(IOException.class));
      assertThat(ex.getMessage(), containsString(resourceName));
      assertSame(res, ex.getResources());
      return;
    }
    fail();
  }

  @Test
  public void testGetPropertiesWithNullStream() {
    Resources res = new Resources(name -> null);
    String resourceName = "null-stream-resource";

    try {
      res.getProperties(resourceName);
    } catch (Resources.ResourceException ex) {
      assertThat(ex.getMessage(), containsString(resourceName));
      assertSame(res, ex.getResources());
      return;
    }
    fail();
  }
}
