package fi.aalto.cs.intellij.common;

import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Version {
  private static final String PROPERTY_KEY = "version";

  public final int major;
  public final int minor;
  public final int build;

  /**
   * Returns a {@link Version} object based on the value of {@value PROPERTY_KEY} in the given
   * properties.
   * @param props A {@link Properties} object, or {@code null} in which case {@code null} is also
   *              returned.
   * @return A {@link Version} object, or {@code null} if the version information could not be
   *         read.
   */
  @Nullable
  public static Version fromProperties(@Nullable Properties props) {
    if (props == null) {
      return null;
    }
    String version = props.getProperty(PROPERTY_KEY);
    return Version.fromString(version);
  }

  /**
   * Returns a {@link Version} object based on the value of the given string.
   * @param version A version string of format "{major}.{minor}.{build}".
   * @return A {@link Version} object, or {@code null} if {@code version} is invalid or null.
   */
  @Nullable
  public static Version fromString(@Nullable String version) {
    if (version == null) {
      return null;
    }

    int major;
    int minor;
    int build;

    String[] parts = version.split("\\.");
    if (parts.length != 3) {
      return null;
    }

    try {
      major = Integer.parseInt(parts[0]);
      minor = Integer.parseInt(parts[1]);
      build = Integer.parseInt(parts[2]);
    } catch (Exception ex) {
      return null;
    }

    return new Version(major, minor, build);
  }

  /**
   * A constructor for {@link Version} class.
   * @param major Major version number.
   * @param minor Minor version number.
   * @param build Number of the build.
   */
  public Version(int major, int minor, int build) {
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
