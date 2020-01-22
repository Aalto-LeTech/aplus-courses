package fi.aalto.cs.intellij.common;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class VersionTest {

  @Test
  public void testCreateVersion() {
    Version version = new Version(3,5,24);

    assertEquals(3, version.major);
    assertEquals(5, version.minor);
    assertEquals(24, version.build);

    assertEquals("3.5.24", version.toString());

    Version sameVersion = new Version(3, 5, 24);
    Version differentVersion = new Version(4, 6, 78);

    assertEquals(version, sameVersion);
    assertEquals(version.hashCode(), sameVersion.hashCode());
    assertNotEquals(version, differentVersion);
  }

  @Test
  public void testCreateVersionWithNegativeNumbers() {
    int failureCount = 0;

    try {
      new Version(7, 0, -5);
    } catch (IllegalArgumentException ex) {
      failureCount++;
    }

    try {
      new Version(3, -13, 1);
    } catch (IllegalArgumentException ex) {
      failureCount++;
    }

    try {
      new Version(-1, 4, 5);
    } catch (IllegalArgumentException ex) {
      failureCount++;
    }

    assertEquals(3, failureCount);
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
    String versionString = "invalid_version";

    try {
      Version.fromString(versionString);
    } catch (IllegalArgumentException ex) {
      assertThat(ex.getMessage(), containsString(versionString));
    }
  }

  @Test
  public void testCreateVersionFromDifferentInvalidStrings() {
    int failureCount = 0;

    try {
      Version.fromString("1.2");
    } catch (IllegalArgumentException ex) {
      failureCount++;
    }

    try {
      Version.fromString("1.2.");
    } catch (IllegalArgumentException ex) {
      failureCount++;
    }

    try {
      Version.fromString("1.2.A");
    } catch (IllegalArgumentException ex) {
      failureCount++;
    }

    try {
      Version.fromString("");
    } catch (IllegalArgumentException ex) {
      failureCount++;
    }

    assertEquals(4, failureCount);
  }
}
