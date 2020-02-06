package fi.aalto.cs.intellij.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that provides methods to read the resources.
 */
public class Resources {

  /**
   * Instance of {@link Resources} that uses the class loader of its own class.
   */
  public static final Resources DEFAULT = new Resources(Resources.class.getClassLoader());

  @NotNull
  private final ResourceProvider resourceProvider;

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
   * Returns properties from a resource.
   * @param resourceName Name of the resource.
   * @return Contents of the resource as a {@link Properties} object.
   * @throws ResourceException If properties could not be read either because the resource was not
   *                           found or it was badly formatted.
   */
  @NotNull
  public Properties getProperties(@NotNull String resourceName) throws ResourceException {
    Properties props = new Properties();
    try (InputStream stream = getStream(resourceName)) {
      props.load(stream);
    } catch (IOException ex) {
      throw new ResourceException(resourceName, "Resource could not be parsed to properties.", ex);
    }
    return props;
  }

  /**
   * Returns a resource as a stream.
   * @param resourceName Name of the resource.
   * @return An {@link InputStream} to the given resource.
   * @throws ResourceException If the resource could not be found.
   */
  @NotNull
  public InputStream getStream(@NotNull String resourceName) throws ResourceException {
    InputStream stream = resourceProvider.getResourceAsStream(resourceName);
    if (stream == null) {
      throw new ResourceException(resourceName, "The resource could not be found.", null);
    }
    return stream;
  }

  /**
   * An abstract interface for an object that provides resources as input streams.
   * The most useful realization of this interface is {@code ClassLoader::getResourceAsStream}.
   */
  @FunctionalInterface
  public interface ResourceProvider {

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
