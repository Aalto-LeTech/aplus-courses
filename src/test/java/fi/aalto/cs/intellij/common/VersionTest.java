package fi.aalto.cs.intellij.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class VersionTest {

  @Test
  public void testCreateVersion() {
    Version version = new Version(3,5,24);

    assertEquals("The major version should be the same as that given to the constructor",
        3, version.major);
    assertEquals("The minor version should be the same as that given to the constructor",
        5, version.minor);
    assertEquals("The build number should be the same as that given to the constructor",
        24, version.build);

    assertEquals("toString() should return the version in format '{major}.{minor}.{build}'.",
        "3.5.24", version.toString());

    Version sameVersion = new Version(3, 5, 24);
    assertEquals("Version should equal to another version created with the same arguments.",
        version, sameVersion);
    assertEquals("Two equal versions should give the same hash code.",
        version.hashCode(), sameVersion.hashCode());

    Version differentMajor = new Version(3, 6, 24);
    assertNotEquals("Versions with different majors versions should not be equal",
        version, differentMajor);

    Version differentMinor = new Version(4, 5, 24);
    assertNotEquals("Versions with different minor versions should not be equal",
        version, differentMinor);

    Version differentBuild = new Version(3, 5, 78);
    assertNotEquals("Versions with different build numbers should not be equal",
        version, differentBuild);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateVersionWithNegativeMajorVersion() {
    new Version(-1, 4, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateVersionWithNegativeMinorVersion() {
    new Version(3, -13, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateVersionWithNegativeBuildNumber() {
    new Version(7, 0, -5);
  }

  @Test
  public void testEmptyVersion() {
    Version version = Version.EMPTY;

    assertEquals(0, version.major);
    assertEquals(0, version.minor);
    assertEquals(0, version.build);
  }

  @Test
  public void testCreateVersionFromValidString() {
    Version version = Version.fromString("14.2.100");

    assertEquals("Version created by fromString(\"14.2.100\") should have major version 4.",
        14, version.major);
    assertEquals("Version created by fromString(\"14.2.100\") should have minor version 2.",
        2, version.minor);
    assertEquals("Version created by fromString(\"14.2.100\") should have build number 100.",
        100, version.build);
  }

  @Test
  public void testCreateVersionFromInvalidString() {
    String versionString = "invalid_version";

    try {
      Version.fromString(versionString);
    } catch (Version.InvalidVersionStringException ex) {
      assertEquals("Exception should have version string that was tried to parse.",
          versionString, ex.getVersionString());
      return;
    }
    fail("fromString() should throw an InvalidVersionStringException if the string is invalid.");
  }

  @Test(expected = Version.InvalidVersionStringException.class)
  public void testCreateVersionFromStringMissingPart() {
    Version.fromString("1.2");
  }

  @Test(expected = Version.InvalidVersionStringException.class)
  public void testCreateVersionFromStringMissingNumber() {
    Version.fromString("9.5.");
  }

  @Test(expected = Version.InvalidVersionStringException.class)
  public void testCreateVersionFromStringWithNonNumericPart() {
    Version.fromString("6.XVII.188");
  }

  @Test(expected = Version.InvalidVersionStringException.class)
  public void testCreateVersionFromEmptyString() {
    Version.fromString("");
  }
}
