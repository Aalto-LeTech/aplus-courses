package fi.aalto.cs.intellij.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.Test;

public class ResourcesTest {

  @Test
  public void testGetProperties() {
    final MockInputStream stream = new MockInputStream("a=x\nb=y\n");

    Resources res = new Resources(name -> {
      assertEquals("correct-properties", name);
      return stream;
    });

    assertFalse(stream.hasCloseMethodBeenCalled);

    Properties props = res.getProperties("correct-properties");

    assertTrue(stream.hasCloseMethodBeenCalled);

    assertNotNull(props);
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

    Properties props = res.getProperties("inaccessible-resource");
    assertNull(props);
  }

  @Test
  public void testGetPropertiesWithNullStream() {
    Resources res = new Resources(name -> null);

    Properties props = res.getProperties("null-stream-resource");
    assertNull(props);
  }

  private static class MockInputStream extends ByteArrayInputStream {
    boolean hasCloseMethodBeenCalled = false;

    public MockInputStream(String content) {
      super(content.getBytes());
    }

    @Override
    public void close() throws IOException {
      super.close();
      hasCloseMethodBeenCalled = true;
    }
  }
}
