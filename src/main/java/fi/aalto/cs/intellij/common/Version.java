package fi.aalto.cs.intellij.common;

import org.jetbrains.annotations.NotNull;

public class Version {
  public static final Version EMPTY = new Version(0, 0, 0);

  public final int major;
  public final int minor;
  public final int build;

  /**
   * Returns a {@link Version} object based on the value of the given string.
   * @param version A version string of format "{major}.{minor}.{build}".
   * @return A {@link Version} object.
   * @throws IllegalArgumentException If the given string is invalid.
   */
  @NotNull
  public static Version fromString(@NotNull String version) {
    int major;
    int minor;
    int build;

    String[] parts = version.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("'" + version + "' does not match the pattern.");
    }

    try {
      major = Integer.parseInt(parts[0]);
      minor = Integer.parseInt(parts[1]);
      build = Integer.parseInt(parts[2]);
      return new Version(major, minor, build);

    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * A constructor for {@link Version} class.
   * @param major Major version number.
   * @param minor Minor version number.
   * @param build Number of the build.
   * @throws IllegalArgumentException If any of the arguments are negative.
   */
  public Version(int major, int minor, int build) {
    if (major < 0 || minor < 0 || build < 0) {
      throw new IllegalArgumentException("All the arguments must be non-negative.");
    }
    this.major = major;
    this.minor = minor;
    this.build = build;
  }

  @Override
  @NotNull
  public String toString() {
    return major + "." + minor + "." + build;
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
