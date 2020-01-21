package fi.aalto.cs.intellij.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;
import org.junit.Test;

public class VersionTest {

  @Test
  public void testCreateVersion() {
    Version version = new Version(3,5,24);

    assertEquals(3, version.major);
    assertEquals(5, version.minor);
    assertEquals(24, version.build);

    String versionString = "3.5.24";
    assertEquals(versionString, version.toString());
    assertEquals(versionString.hashCode(), version.hashCode());

    Version sameVersion = new Version(3, 5, 24);
    Version differentVersion = new Version(4, 6, 78);

    assertEquals(version, sameVersion);
    assertNotEquals(version, differentVersion);
    assertNotEquals(version, versionString);
  }

  @Test
  public void testCreateVersionFromValidString() {
    Version version = Version.fromString("14.2.100");

    assertNotNull(version);
    assertEquals(14, version.major);
    assertEquals(2, version.minor);
    assertEquals(100, version.build);
  }

  @Test
  public void testCreateVersionFromAllZeroString() {
    Version version = Version.fromString("0.0.0");

    assertNotNull(version);
    assertEquals(0, version.major);
    assertEquals(0, version.minor);
    assertEquals(0, version.build);
  }

  @Test
  public void testCreateVersionFromInvalidString() {
    assertNull(Version.fromString("1.2"));
    assertNull(Version.fromString("1.2."));
    assertNull(Version.fromString("1.2.A"));
    assertNull(Version.fromString(""));
  }

  @Test
  public void testCreateVersionFromValidProperties() {
    String versionString = "7.2.1342";
    Properties props = new Properties();
    props.setProperty("version", versionString);

    Version version = Version.fromProperties(props);

    assertNotNull(version);
    assertEquals(versionString, version.toString());
  }

  @Test
  public void testCreateVersionFromInvalidProperties() {
    Properties props = new Properties();
    props.setProperty("not-version", "1.0.0");

    assertNull(Version.fromProperties(props));
    assertNull(Version.fromProperties(null));

  }
}
