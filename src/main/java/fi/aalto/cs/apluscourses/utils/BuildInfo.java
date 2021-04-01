package fi.aalto.cs.apluscourses.utils;

import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information that was gathered during the build of the program (such as version).
 * Normally, this class should be accessed via {@code BuildInfo.INSTANCE}.
 */
public class BuildInfo {

  private static final Logger logger = LoggerFactory.getLogger(BuildInfo.class);

  private static final String RESOURCE_NAME = "build-info.properties";

  public static final BuildInfo INSTANCE;

  @NotNull
  public final Version version;

  @NotNull
  public final Version courseVersion;

  static {
    BuildInfo buildInfo = null;
    try {
      buildInfo = new BuildInfo(Resources.DEFAULT.getProperties(RESOURCE_NAME));
    } catch (ResourceException ex) {
      logger.error("Could not read build info from resources.", ex);
    } catch (PropertyException ex) {
      logger.error("Build info is badly formatted.", ex);
    }
    INSTANCE = buildInfo == null ? new BuildInfo() : buildInfo;
  }

  /**
   * Constructs an "empty" {@link BuildInfo} object to be used as default value if a proper build
   * info is not available.
   */
  BuildInfo() {
    version = Version.EMPTY;
    courseVersion = Version.EMPTY;
  }

  /**
   * Construct a {@link BuildInfo} from given properties.
   * @param properties Properties containing the build information.
   * @throws PropertyException If the properties are not valid.
   */
  BuildInfo(@NotNull Properties properties) throws PropertyException {
    PropertyReader reader = new PropertyReader(properties);
    version = reader.getPropertyAsObject(PropertyKeys.VERSION, Version::fromString);
    courseVersion = reader.getPropertyAsObject(PropertyKeys.COURSE_VERSION, Version::fromString);
  }

  static class PropertyKeys {
    public static final String VERSION = "version";

    public static final String COURSE_VERSION = "courseVersion";

    private PropertyKeys() {

    }
  }
}
