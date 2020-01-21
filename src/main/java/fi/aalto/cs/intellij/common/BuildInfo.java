package fi.aalto.cs.intellij.common;

import java.util.Properties;
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

  public static class SingletonUtility {
    public static final BuildInfo INSTANCE = new BuildInfo(Resources.SingletonUtility.INSTANCE);

    private SingletonUtility() { }
  }
}
