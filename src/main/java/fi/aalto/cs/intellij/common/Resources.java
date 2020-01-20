package fi.aalto.cs.intellij.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that provides methods to read the resource files of the plugin application.
 * Normally, this class should be accessed via {@code Resources.SingletonUtility.INSTANCE}.
 */
class Resources {

  public static final Logger LOGGER = LoggerFactory.getLogger(Resources.class);

  private ResourceProvider resourceProvider;

  Resources(@NotNull ClassLoader classLoader) {
    this(classLoader::getResourceAsStream);
  }

  Resources(@NotNull ResourceProvider resourceProvider) {
    this.resourceProvider = resourceProvider;
  }

  /**
   * Returns properties from a properties resource file.
   * @param resourceName Name of a resource.
   * @return Contents of the resource in a {@link Properties} object.
   */
  @Nullable
  public Properties getProperties(@NotNull String resourceName) {
    try (InputStream stream = resourceProvider.getResourceAsStream(resourceName)) {
      return getProperties(stream);
    } catch (Exception ex) {
      LOGGER.error("Could not read a resource: " + resourceName, ex);
      return null;
    }
  }

  @Nullable
  private Properties getProperties(@Nullable InputStream stream) throws IOException {
    if (stream == null) {
      LOGGER.error("Stream is null.");
      return null;
    }
    Properties props = new Properties();
    props.load(stream);
    return props;
  }

  interface ResourceProvider {
    /**
     * Opens an input stream to the resource indicated by {@code name}.
     * @param name Name of the resource.
     * @return An open {@link InputStream} which corresponds to {@code name} or {@code null} if
     *         the stream could not be opened.
     */
    @Nullable
    InputStream getResourceAsStream(@NotNull String name);
  }

  public static class SingletonUtility {
    public static final Resources INSTANCE = new Resources(Resources.class.getClassLoader());

    private SingletonUtility() { }
  }
}
