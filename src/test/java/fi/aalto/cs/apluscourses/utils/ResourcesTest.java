package fi.aalto.cs.apluscourses.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourcesTest {

  @Test
  void testGetProperties() throws ResourceException {
    AtomicInteger closeCallCounter = new AtomicInteger(0);

    Resources res = new Resources(name -> {
      Assertions.assertEquals("correct-properties", name);

      return new ByteArrayInputStream("a=x\nb=y\n".getBytes()) {

        @Override
        public void close() throws IOException {
          closeCallCounter.getAndIncrement();
          super.close();
        }
      };
    });

    Assertions.assertEquals(0, closeCallCounter.get(), "Stream should not be closed by the constructor.");

    Properties props = res.getProperties("correct-properties");

    Assertions.assertEquals(1, closeCallCounter.get(), "Stream should be closed by the getProperties().");
    Assertions.assertEquals("x", props.getProperty("a"), "getProperty(\"a\") should return the value set for 'a'");
    Assertions.assertEquals("y", props.getProperty("b"), "getProperty(\"b\") should return the value set for 'b'");
  }

  @Test
  void testGetPropertiesWithError() {
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
      Assertions.assertEquals(resourceName, ex.getResourceName(), "Exception should have the requested resource name.");
      Assertions.assertSame(exception, ex.getCause(), "The cause of the expression should come from the stream.");
      return;
    }
    Assertions.fail("getProperties() should throw an exception if the stream cannot be read.");
  }

  @Test
  void testGetPropertiesWithNullStream() {
    Resources res = new Resources(name -> null);
    String resourceName = "null-stream-resource";

    try {
      res.getProperties(resourceName);
    } catch (ResourceException ex) {
      Assertions.assertEquals(resourceName, ex.getResourceName(), "Exception should have the requested resource name.");
      return;
    }
    Assertions.fail("getProperties() should throw an exception if the stream is null");
  }

  @Test
  void testGetImage() throws ResourceException, IOException {
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

    Assertions.assertEquals(1, image.getHeight(), "Height of the image should be 1");
    Assertions.assertEquals(1, image.getWidth(), "Width of the image should be 1");
    Assertions.assertEquals(0xffff0000, image.getRGB(0, 0), "The only pixel should be red");
  }

  @Test
  void testGetImageWithError() throws IOException, ResourceException {
    ByteArrayInputStream stream = spy(new ByteArrayInputStream("not-an-image".getBytes()));
    Resources.ResourceProvider resourceProvider = mock(Resources.ResourceProvider.class);
    when(resourceProvider.getResourceAsStream("badTestImage")).thenReturn(stream);

    Resources res = new Resources(resourceProvider);
    try {
      assertThrows(ResourceException.class, () ->
          res.getImage("badTestImage"));
    } finally {
      verify(stream).close();
    }
  }

  @Test
  void testGetIconWithNullStream() {
    Resources res = new Resources(name -> null);
    String resourceName = "null-stream-resource";

    try {
      res.getImage(resourceName);
    } catch (ResourceException ex) {
      Assertions.assertEquals(resourceName, ex.getResourceName(), "Exception should have the requested resource name.");
      return;
    }
    Assertions.fail("getImage() should throw an exception if the stream is null");
  }
}
