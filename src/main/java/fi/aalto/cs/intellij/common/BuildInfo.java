package fi.aalto.cs.intellij.common;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information that was gathered during the build of the program (such as version).
 * Normally, this class should be accessed via {@code BuildInfo.SingletonUtility.INSTANCE}.
 */
public class BuildInfo {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildInfo.class);
  private static final String RESOURCE_NAME = "build-info.properties";

  public final Version version;

  BuildInfo(@Nullable Properties props) {
    Version ver = Version.fromProperties(props);
    if (ver == null) {
      ver = new Version(0,0,0);
      LOGGER.error("Version information is invalid.  '{}' is used instead.", ver);
    }
    this.version = ver;
  }

  BuildInfo(@NotNull Resources resources) {
    this(resources.getProperties(RESOURCE_NAME));
  }

  public static class Version {
    private static final String PROPERTY_KEY = "version";
    private static final Pattern PATTERN = Pattern.compile("(\\d{1,9})\\.(\\d{1,9})\\.(\\d{1,9})");

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
      Matcher m = PATTERN.matcher(version);
      if (m.matches()) {
        return new Version(
            Integer.parseInt(m.group(1)),
            Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(3)));
      }
      return null;
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
      return obj instanceof BuildInfo.Version && toString().equals(obj.toString());
    }
  }

  public static class SingletonUtility {
    public static final BuildInfo INSTANCE = new BuildInfo(Resources.SingletonUtility.INSTANCE);

    private SingletonUtility() { }
  }
}
