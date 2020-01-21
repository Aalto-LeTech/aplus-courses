package fi.aalto.cs.intellij.common;

import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

  @Test
  public void testCreateVersion() {
    String versionString = "3.5.24";
    Version version = new Version(3,5,24);

    Assert.assertEquals(3, version.major);
    Assert.assertEquals(5, version.minor);
    Assert.assertEquals(24, version.build);
    Assert.assertEquals(versionString, version.toString());
    Assert.assertEquals(versionString.hashCode(), version.hashCode());

    Version sameVersion = new Version(3, 5, 24);
    Version differentVersion = new Version(4, 6, 78);

    Assert.assertEquals(version, sameVersion);
    Assert.assertNotEquals(version, differentVersion);
    Assert.assertNotEquals(version, versionString);
  }

  @Test
  public void testCreateVersionFromValidString() {
    Version version = Version.fromString("14.2.100");

    Assert.assertNotNull(version);
    Assert.assertEquals(14, version.major);
    Assert.assertEquals(2, version.minor);
    Assert.assertEquals(100, version.build);
  }

  @Test
  public void testCreateVersionFromAllZeroString() {
    Version version = Version.fromString("0.0.0");

    Assert.assertNotNull(version);
    Assert.assertEquals(0, version.major);
    Assert.assertEquals(0, version.minor);
    Assert.assertEquals(0, version.build);
  }

  @Test
  public void testCreateVersionFromInvalidString() {
    Version version = Version.fromString("1.2.");

    Assert.assertNull(version);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testCreateVersionFromNullString() {
    Version version = Version.fromString(null);

    Assert.assertNull(version);
  }

  @Test
  public void testCreateVersionFromValidProperties() {
    String versionString = "7.2.1342";
    Properties props = new Properties();
    props.setProperty("version", versionString);

    Version version = Version.fromProperties(props);

    Assert.assertNotNull(version);
    Assert.assertEquals(versionString, version.toString());
  }

  @Test
  public void testCreateVersionFromPropertiesWithoutKey() {
    Properties props = new Properties();
    props.setProperty("not-version", "1.0.0");

    Version version = Version.fromProperties(props);

    Assert.assertNull(version);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testCreateVersionFromNullProperties() {
    Version version = Version.fromProperties(null);

    Assert.assertNull(version);
  }
}
