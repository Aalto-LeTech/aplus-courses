package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.diagnostic.Logger;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

/**
 * Holds information that was gathered during the build of the program (such as version).
 * Normally, this class should be accessed via {@code BuildInfo.INSTANCE}.
 */
public class BuildInfo {

  private static final Logger logger = APlusLogger.logger;

  private static final String RESOURCE_NAME = "build-info.properties";

  public static final BuildInfo INSTANCE;

  @NotNull
  public final Version pluginVersion;

  @NotNull
  public final Version courseVersion;

  static {
    BuildInfo buildInfo = null;
    try {
      buildInfo = new BuildInfo(Resources.DEFAULT.getProperties(RESOURCE_NAME));
    } catch (ResourceException ex) {
      logger.warn("Could not read build info from resources.", ex);
    } catch (PropertyException ex) {
      logger.warn("Build info is badly formatted.", ex);
    }
    INSTANCE = buildInfo == null ? new BuildInfo() : buildInfo;
  }

  /**
   * Constructs an "empty" {@link BuildInfo} object to be used as default value if a proper build
   * info is not available.
   */
  BuildInfo() {
    pluginVersion = Version.EMPTY;
    courseVersion = Version.EMPTY;
  }

  /**
   * Construct a {@link BuildInfo} from given properties.
   *
   * @param properties Properties containing the build information.
   * @throws PropertyException If the properties are not valid.
   */
  BuildInfo(@NotNull Properties properties) throws PropertyException {
    PropertyReader reader = new PropertyReader(properties);
    pluginVersion = reader.getPropertyAsObject(PropertyKeys.VERSION, Version::fromString);
    courseVersion = reader.getPropertyAsObject(PropertyKeys.COURSE_VERSION, Version::fromString);
  }

  static class PropertyKeys {
    public static final String VERSION = "version";

    public static final String COURSE_VERSION = "courseVersion";

    private PropertyKeys() {

    }
  }
}
