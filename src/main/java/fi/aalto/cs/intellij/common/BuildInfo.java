package fi.aalto.cs.intellij.common;

import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information that was gathered during the build of the program (such as version).
 * Normally, this class should be accessed via {@code BuildInfo.INSTANCE}.
 */
public class BuildInfo {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildInfo.class);
  private static final String RESOURCE_NAME = "build-info.properties";
  public static final BuildInfo INSTANCE;

  public final Version version;

  static {
    BuildInfo buildInfo;
    try {
      Resources resources = new Resources(BuildInfo.class.getClassLoader());
      buildInfo = new  BuildInfo(resources.getProperties(RESOURCE_NAME));
    } catch (Exception ex) {
      LOGGER.error("Could not read build info.");
      buildInfo = new BuildInfo();
    }
    INSTANCE = buildInfo;
  }

  /**
   * Constructs an "empty" {@link BuildInfo} object to be used as default value if a proper build
   * info is not available.
   */
  BuildInfo() {
    version = Version.EMPTY;
  }

  /**
   * Construct a {@link BuildInfo} from given properties.
   * @param properties Properties containing the build information.
   * @throws BuildInfoException If the properties do not contain necessary information.
   */
  BuildInfo(@NotNull Properties properties) throws BuildInfoException {
    PropertiesReader reader = new PropertiesReader(properties);
    try {
      version = Version.fromString(reader.getProperty(PropertyKeys.VERSION));
    } catch (Exception ex) {
      throw new BuildInfoException("Properties do not contain necessary information.", ex);
    }
  }

  public static class BuildInfoException extends Exception {

    public BuildInfoException(@NotNull String s, @Nullable Throwable throwable) {
      super(s, throwable);
    }
  }

  static class PropertyKeys {
    public static final String VERSION = "version";
  }
}
