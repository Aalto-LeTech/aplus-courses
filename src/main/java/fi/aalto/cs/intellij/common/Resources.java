package fi.aalto.cs.intellij.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Resources {

  public static final Logger LOGGER = LoggerFactory.getLogger(Resources.class);
  public static final Resources INSTANCE = new Resources(Resources.class.getClassLoader());

  private ResourceProvider resourceProvider;

  Resources(@NotNull ClassLoader classLoader) {
    this(classLoader::getResourceAsStream);
  }

  Resources(@NotNull ResourceProvider resourceProvider) {
    this.resourceProvider = resourceProvider;
  }

  public Properties getProperties(@NotNull String resourceName) {
    Properties props = new Properties();
    try (InputStream stream = resourceProvider.getResourceAsStream(resourceName)) {
      if (stream == null) {
        throw new IOException("Could not open a stream.");
      }
      props.load(stream);
    } catch (IOException ex) {
      LOGGER.error("Could not read a resource.", ex);
      return null;
    }
    return props;
  }

  public IdeaPluginDescriptor getPluginDescriptor() {
    return null;
  }

  interface ResourceProvider {
    /**
     * Opens an input stream to the resource indicated by {@code name}.
     * @param name Name of the resource.
     * @return An open {@link InputStream} which corresponds to {@code name}.
     */
    InputStream getResourceAsStream(@NotNull String name);
  }
}
