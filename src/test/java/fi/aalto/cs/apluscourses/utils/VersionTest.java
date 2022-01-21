package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VersionTest {

  @Test
  void testCreateVersion() {
    Version version = new Version(3, 5);

    Assertions.assertEquals(3, version.major, "The major version should be the same as that given to the constructor");
    Assertions.assertEquals(5, version.minor, "The minor version should be the same as that given to the constructor");

    Assertions.assertEquals("3.5", version.toString(),
        "toString() should return the version in format '{major}.{minor}'.");

    Version sameVersion = new Version(3, 5);
    Assertions.assertEquals(version, sameVersion,
        "Version should equal to another version created with the same arguments.");
    Assertions.assertEquals(version.hashCode(), sameVersion.hashCode(),
        "Two equal versions should give the same hash code.");

    Version differentMajor = new Version(3, 6);
    Assertions.assertNotEquals(version, differentMajor, "Versions with different majors versions should not be equal");

    Version differentMinor = new Version(4, 5);
    Assertions.assertNotEquals(version, differentMinor, "Versions with different minor versions should not be equal");
  }

  @Test
  void testCreateVersionWithNegativeMajorVersion() {
    assertThrows(IllegalArgumentException.class, () ->
        new Version(-1, 4));
  }

  @Test
  void testCreateVersionWithNegativeMinorVersion() {
    assertThrows(IllegalArgumentException.class, () ->
        new Version(3, -13));
  }

  @Test
  void testEmptyVersion() {
    Version version = Version.EMPTY;

    Assertions.assertEquals(0, version.major);
    Assertions.assertEquals(0, version.minor);
  }

  @Test
  void testCreateVersionFromValidString() {
    Version version = Version.fromString("14.2");

    Assertions.assertEquals(14, version.major, "Version created by fromString(\"14.2\") should have major version 4.");
    Assertions.assertEquals(2, version.minor, "Version created by fromString(\"14.2\") should have minor version 2.");
  }

  @Test
  void testCreateVersionFromInvalidString() {
    String versionString = "invalid_version";

    try {
      Version.fromString(versionString);
    } catch (Version.InvalidVersionStringException ex) {
      Assertions.assertEquals(versionString, ex.getVersionString(),
          "Exception should have version string that was tried to parse.");
      return;
    }
    Assertions.fail("fromString() should throw an InvalidVersionStringException if the string is invalid.");
  }

  @Test
  void testCreateVersionFromStringMissingPart() {
    assertThrows(Version.InvalidVersionStringException.class, () ->
        Version.fromString("1"));
  }

  @Test
  void testCreateVersionFromStringMissingNumber() {
    assertThrows(Version.InvalidVersionStringException.class, () ->
        Version.fromString("9."));
  }

  @Test
  void testCreateVersionFromStringWithNonNumericPart() {
    assertThrows(Version.InvalidVersionStringException.class, () ->
        Version.fromString("6.XVII"));
  }

  @Test
  void testCreateVersionFromEmptyString() {
    assertThrows(Version.InvalidVersionStringException.class, () ->
        Version.fromString(""));
  }
}
