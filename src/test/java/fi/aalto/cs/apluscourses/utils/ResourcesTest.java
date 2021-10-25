package fi.aalto.cs.apluscourses.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

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
  public void testGetImage() throws ResourceException, IOException {
    // Example PNG, one red pixel
    // https://en.wikipedia.org/wiki/Portable_Network_Graphics#Examples
    byte[] imageData = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
        (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
        0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, (byte) 0xC0, 0x00,
        0x00, 0x03, 0x01, 0x01, 0x00, 0x18, (byte) 0xDD, (byte) 0x8D,
        (byte) 0xB0, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E,
        0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    ByteArrayInputStream stream = spy(new ByteArrayInputStream(imageData));
    Resources.ResourceProvider resourceProvider = mock(Resources.ResourceProvider.class);
    when(resourceProvider.getResourceAsStream("testImage")).thenReturn(stream);

    Resources res = new Resources(resourceProvider);
    BufferedImage image = res.getImage("testImage");
    verify(stream).close();

    assertEquals("Height of the image should be 1", 1, image.getHeight());
    assertEquals("Width of the image should be 1", 1, image.getWidth());
    assertEquals("The only pixel should be red", 0xffff0000, image.getRGB(0, 0));
  }

  @Test(expected = ResourceException.class)
  public void testGetImageWithError() throws IOException, ResourceException {
    ByteArrayInputStream stream = spy(new ByteArrayInputStream("not-an-image".getBytes()));
    Resources.ResourceProvider resourceProvider = mock(Resources.ResourceProvider.class);
    when(resourceProvider.getResourceAsStream("badTestImage")).thenReturn(stream);

    Resources res = new Resources(resourceProvider);
    try {
      res.getImage("badTestImage");
    } finally {
      verify(stream).close();
    }
  }

  @Test
  public void testGetIconWithNullStream() {
    Resources res = new Resources(name -> null);
    String resourceName = "null-stream-resource";

    try {
      res.getImage(resourceName);
    } catch (ResourceException ex) {
      assertEquals("Exception should have the requested resource name.",
          resourceName, ex.getResourceName());
      return;
    }
    fail("getImage() should throw an exception if the stream is null");
  }
}
