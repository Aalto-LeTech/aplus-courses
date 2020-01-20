package fi.aalto.cs.intellij.common;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

public class BuildInfoTest {

  @Test
  public void testCreateVersion() {
    String versionString = "3.5.24";
    BuildInfo.Version version = new BuildInfo.Version(3,5,24);

    Assert.assertEquals(3, version.major);
    Assert.assertEquals(5, version.minor);
    Assert.assertEquals(24, version.build);
    Assert.assertEquals(versionString, version.toString());
    Assert.assertEquals(versionString.hashCode(), version.hashCode());

    BuildInfo.Version sameVersion = new BuildInfo.Version(3, 5, 24);
    BuildInfo.Version differentVersion = new BuildInfo.Version(4, 6, 78);

    Assert.assertEquals(version, sameVersion);
    Assert.assertNotEquals(version, differentVersion);
    Assert.assertNotEquals(version, versionString);
  }

  @Test
  public void testCreateVersionFromValidString() {
    BuildInfo.Version version = BuildInfo.Version.fromString("14.2.100");

    Assert.assertNotNull(version);
    Assert.assertEquals(14, version.major);
    Assert.assertEquals(2, version.minor);
    Assert.assertEquals(100, version.build);
  }

  @Test
  public void testCreateVersionFromAllZeroString() {
    BuildInfo.Version version = BuildInfo.Version.fromString("0.0.0");

    Assert.assertNotNull(version);
    Assert.assertEquals(0, version.major);
    Assert.assertEquals(0, version.minor);
    Assert.assertEquals(0, version.build);
  }

  @Test
  public void testCreateVersionFromInvalidString() {
    BuildInfo.Version version = BuildInfo.Version.fromString("1.2.");

    Assert.assertNull(version);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testCreateVersionFromNullString() {
    BuildInfo.Version version = BuildInfo.Version.fromString(null);

    Assert.assertNull(version);
  }

  @Test
  public void testCreateVersionFromValidProperties() {
    String versionString = "7.2.1342";
    Properties props = new Properties();
    props.setProperty("version", versionString);

    BuildInfo.Version version = BuildInfo.Version.fromProperties(props);

    Assert.assertNotNull(version);
    Assert.assertEquals(versionString, version.toString());
  }

  @Test
  public void testCreateVersionFromPropertiesWithoutKey() {
    Properties props = new Properties();
    props.setProperty("not-version", "1.0.0");

    BuildInfo.Version version = BuildInfo.Version.fromProperties(props);

    Assert.assertNull(version);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testCreateVersionFromNullProperties() {
    BuildInfo.Version version = BuildInfo.Version.fromProperties(null);

    Assert.assertNull(version);
  }

  @Test
  public void testCreateBuildInfoFromResources() {
    Resources res = new Resources(name -> {
      Assert.assertEquals("build-info.properties", name);
      return new ByteArrayInputStream("version=1.5.18\n".getBytes());
    });

    BuildInfo buildInfo = new BuildInfo(res);

    Assert.assertEquals("1.5.18", buildInfo.version.toString());
  }

  @Test
  public void testCreateBuildInfoFromResourcesMissing() {
    Resources res = new Resources(name -> null);

    BuildInfo buildInfo = new BuildInfo(res);

    Assert.assertEquals("0.0.0", buildInfo.version.toString());
  }
}
