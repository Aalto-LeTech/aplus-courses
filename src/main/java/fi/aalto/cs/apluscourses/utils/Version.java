package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Version {

  public static final Version EMPTY = new Version(0, 0);

  public final int major;
  public final int minor;

  /**
   * Returns a {@link Version} object based on the value of the given string.
   * @param versionString A version string of format "{major}.{minor}.{build}".
   * @return A {@link Version} object.
   * @throws InvalidVersionStringException If the given string is invalid.
   */
  @NotNull
  public static Version fromString(@NotNull String versionString) {
    int major;
    int minor;

    String[] parts = versionString.split("\\.");
    if (parts.length != 2) {
      throw new InvalidVersionStringException(versionString, null);
    }

    try {
      major = Integer.parseInt(parts[0]);
      minor = Integer.parseInt(parts[1]);
      return new Version(major, minor);
    } catch (NumberFormatException ex) {
      throw new InvalidVersionStringException(versionString, ex);
    }
  }

  public static class InvalidVersionStringException extends RuntimeException {

    @NotNull
    private final String versionString;

    public InvalidVersionStringException(@NotNull String versionString, @Nullable Throwable cause) {
      super("Version string '" + versionString + "' does not match the expected pattern.", cause);
      this.versionString = versionString;
    }

    @NotNull
    public String getVersionString() {
      return versionString;
    }
  }

  /**
   * A constructor for {@link Version} class.
   * @param major Major version number.
   * @param minor Minor version number.
   * @throws IllegalArgumentException If any of the arguments are negative.
   */
  public Version(int major, int minor) {
    if (major < 0 || minor < 0) {
      throw new IllegalArgumentException("All the parts of version number must be non-negative.");
    }
    this.major = major;
    this.minor = minor;
  }

  @Override
  @NotNull
  public String toString() {
    return major + "." + minor;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Version && toString().equals(obj.toString());
  }
}
