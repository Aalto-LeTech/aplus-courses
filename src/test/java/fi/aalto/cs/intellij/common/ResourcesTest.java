package fi.aalto.cs.intellij.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResourcesTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGetResourcesInstance() {
    Resources res = Resources.getInstance();
    Assert.assertNotNull(res);
    Resources res2 = Resources.getInstance();
    Assert.assertSame(res, res2);
  }

  @Test
  public void testGetPropertiesFromResource() {
    Resources res = new Resources(name -> {
      Assert.assertEquals("correct-properties", name);
      return new ByteArrayInputStream("a=x\nb=y\n".getBytes());
    });

    Properties props = res.getPropertiesFromResource("correct-properties");
    Assert.assertNotNull(props);
    Assert.assertEquals("x", props.getProperty("a"));
    Assert.assertEquals("y", props.getProperty("b"));
  }

  @Test
  public void testGetPropertiesWithNoLuck() {
    Resources res = new Resources(name -> new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException();
      }
    });

    exception.expect(Resources.ResourceException.class);
    exception.expectMessage("Could not access resource: inaccessible-resource");
    res.getPropertiesFromResource("inaccessible-resource");
  }

  @Test
  public void testGetPropertiesNullFailure() {
    Resources res = new Resources(name -> null);

    exception.expect(Resources.ResourceException.class);
    exception.expectMessage("Could not access resource: null-stream-resource");
    res.getPropertiesFromResource("null-stream-resource");
  }

}
