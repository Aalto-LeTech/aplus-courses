package fi.aalto.cs.intellij.common;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildInfo {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildInfo.class);
  private static final String RESOURCE_NAME = "build-info";
  public static final BuildInfo INSTANCE = new BuildInfo(Resources.INSTANCE);

  public final Version version;

  BuildInfo(Properties props) {
    String verStr = props.getProperty("version");
    Version version = Version.fromString(verStr);
    if (version == null) {
      LOGGER.error("Invalid version: '{}'.  '0.0.0' is used instead.", verStr);
      version = new Version(0,0,0);
    }
    this.version = version;
  }

  BuildInfo(Resources resources) {
    this(resources.getProperties(RESOURCE_NAME));
  }

  public static class Version {
    private static final Pattern PATTERN = Pattern.compile("(\\d{1,9})\\.(\\d{1,9})\\.(\\d{1,9})");

    public final int major;
    public final int minor;
    public final int build;

    public static Version fromString(@NotNull String verStr){
      Matcher m = PATTERN.matcher(verStr);
      if (m.matches()) {
        return new Version(
            Integer.parseInt(m.group(1)),
            Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(3)));
      }
      return null;
    }

    public Version(int major, int minor, int build) {
      this.major = major;
      this.minor = minor;
      this.build = build;
    }

    @Override
    public String toString() {
      return String.format("%d.%d.%d", major, minor, build);
    }
  }
}
