package fi.aalto.cs.intellij.common;

import java.io.InputStream;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that provides methods to read the resources.
 */
class Resources {

  private ResourceProvider resourceProvider;

  /**
   * Constructs a new {@link Resources} object that provides an access to resources from a given
   * class loader.
   * @param classLoader A {@link ClassLoader} from which the resources are read.
   */
  public Resources(@NotNull ClassLoader classLoader) {
    this(classLoader::getResourceAsStream);
  }

  /**
   * Constructs a new {@link Resources} object that provides an access to resources from a given
   * resource provider.
   * @param resourceProvider A {@link ResourceProvider} from which the resources are read.
   */
  public Resources(@NotNull ResourceProvider resourceProvider) {
    this.resourceProvider = resourceProvider;
  }

  /**
   * Returns properties from a properties resource file.
   * @param resourceName Name of a resource.
   * @return Contents of the resource in a {@link Properties} object.
   * @throws ResourceException If properties could not be read.
   */
  @NotNull
  public Properties getProperties(@NotNull String resourceName) throws ResourceException {
    try (InputStream stream = resourceProvider.getResourceAsStream(resourceName)) {
      if (stream == null) {
        throw new Exception("Resource could not be found.");
      }
      Properties props = new Properties();
      props.load(stream);
      return props;
    } catch (Exception ex) {
      throw new ResourceException("Could not read a resource: " + resourceName, ex);
    }
  }

  public class ResourceException extends Exception {

    /**
     * Constructs a {@link ResourceException} object representing an error that occurred while
     * trying to read resources.
     * @param message A description of what went wrong.
     * @param cause An {@link Exception} (or other {@link Throwable}) which caused this exception
     *              or, null if there is no such a cause.
     */
    public ResourceException(@NotNull String message, @Nullable Throwable cause) {
      super(message, cause);
    }

    /**
     * Returns the parent object of this exception.
     * @return A {@link Resources} object within which this exception occurred.
     */
    public Resources getResources() {
      return Resources.this;
    }
  }

  /**
   * An abstract interface for an object that provides resources as input streams.
   * The most useful realization of this interface is {@code ClassLoader::getResourceAsStream}.
   */
  @FunctionalInterface
  interface ResourceProvider {
    /**
     * Opens an input stream to the resource indicated by {@code name}.
     * @param name Name of the resource.
     * @return An open {@link InputStream} which corresponds to {@code name} or {@code null} if
     *         the resource could not be found.
     */
    @Nullable
    InputStream getResourceAsStream(@NotNull String name);
  }
}
