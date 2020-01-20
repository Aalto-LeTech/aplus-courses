package fi.aalto.cs.intellij.common;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

public class BuildInfoTest {

  @Test
  public void test_Version() {
    BuildInfo.Version version = new BuildInfo.Version(3,5,24);

    Assert.assertEquals(3, version.major);
    Assert.assertEquals(5, version.minor);
    Assert.assertEquals(24, version.build);
    Assert.assertEquals("3.5.24", version.toString());
  }

  @Test
  public void test_Version_fromString_valid() {
    BuildInfo.Version version = BuildInfo.Version.fromString("3.5.24");

    Assert.assertNotNull(version);
    Assert.assertEquals(3, version.major);
    Assert.assertEquals(5, version.minor);
    Assert.assertEquals(24, version.build);
  }

  @Test
  public void test_Version_fromString_validZeros() {
    BuildInfo.Version version = BuildInfo.Version.fromString("0.0.0");

    Assert.assertNotNull(version);
    Assert.assertEquals(0, version.major);
    Assert.assertEquals(0, version.minor);
    Assert.assertEquals(0, version.build);
  }

  @Test
  public void test_Version_fromString_invalid() {
    BuildInfo.Version version = BuildInfo.Version.fromString("mickey");

    Assert.assertNull(version);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void test_Version_fromString_invalidNull() {
    BuildInfo.Version version = BuildInfo.Version.fromString(null);

    Assert.assertNull(version);
  }

  @Test
  public void test_Version_fromString_invalidPartMissing() {
    BuildInfo.Version version = BuildInfo.Version.fromString("1.2.");

    Assert.assertNull(version);
  }

  @Test
  public void test_Version_fromProperties_valid() {
    Properties props = new Properties();
    props.setProperty("version", "7.2.1342");

    BuildInfo.Version version = BuildInfo.Version.fromProperties(props);

    Assert.assertNotNull(version);
    Assert.assertEquals("7.2.1342", version.toString());
  }

  @Test
  public void test_Version_fromProperties_invalidMissing() {
    Properties props = new Properties();
    props.setProperty("not-version", "1.0.0");

    BuildInfo.Version version = BuildInfo.Version.fromProperties(props);

    Assert.assertNull(version);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void test_Version_fromProperties_invalidNull() {
    BuildInfo.Version version = BuildInfo.Version.fromProperties(null);

    Assert.assertNull(version);
  }

  @Test
  public void test_BuildInfo_fromResources() {
    Resources res = new Resources(name -> {
      Assert.assertEquals("build-info.properties", name);
      return new ByteArrayInputStream("version=1.5.18\n".getBytes());
    });

    BuildInfo buildInfo = new BuildInfo(res);

    Assert.assertEquals("1.5.18", buildInfo.version.toString());
  }

  @Test
  public void test_BuildInfo_fromResourcesButMissing() {
    Resources res = new Resources(name -> null);

    BuildInfo buildInfo = new BuildInfo(res);

    Assert.assertEquals("0.0.0", buildInfo.version.toString());
  }
}
