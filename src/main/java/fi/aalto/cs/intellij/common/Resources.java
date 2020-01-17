package fi.aalto.cs.intellij.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Resources {

  private static final Logger logger = LoggerFactory.getLogger(Resources.class);
  private static Resources INSTANCE;

  private ResourceProvider resourceProvider;

  Resources(@NotNull ResourceProvider resourceProvider) {
    this.resourceProvider = resourceProvider;
  }

  /**
   * Get a singleton instance of {@link Resources} class.
   * @return A singleton {@link Resources} object.
   */
  public static Resources getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Resources(
          name -> Resources.class.getClassLoader().getResourceAsStream(name)
      );
    }
    return INSTANCE;
  }

  public Properties getPropertiesFromResource(@NotNull String resourceName) {
    Properties props = new Properties();
    try (InputStream stream = resourceProvider.getResourceAsStream(resourceName)) {
      if (stream == null) {
        throw new ResourceException(resourceName, new NullPointerException());
      }
      props.load(stream);
    } catch (IOException ex) {
      throw new ResourceException(resourceName, ex);
    }
    return props;
  }


  static class ResourceException extends RuntimeException {
    public ResourceException(String resourceName, Throwable cause) {
      super("Could not access resource: " + resourceName, cause);
    }
  }

  interface ResourceProvider {
    /**
     * Opens an input stream to the resource indicated by {@code name}.
     * @param name Name of the resource.
     * @return An open
     */
    InputStream getResourceAsStream(@NotNull String name);
  }
}
