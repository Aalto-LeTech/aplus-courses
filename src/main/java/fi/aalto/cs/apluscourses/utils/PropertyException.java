package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * A class for exceptions related to a certain property.
 */
public class PropertyException extends Exception {

  @NotNull
  private final Properties properties;
  @NotNull
  private final String propertyKey;

  /**
   * Constructs a {@link PropertyException}.
   * @param propertyKey The key of the property to which this exception relates.
   * @param message The message.
   * @param cause The cause of this exception or null.
   */
  public PropertyException(@NotNull Properties properties,
                           @NotNull String propertyKey,
                           @NotNull String message,
                           @Nullable Throwable cause) {
    super("Property '" + propertyKey + "': " + message, cause);

    this.properties = properties;
    this.propertyKey = propertyKey;
  }

  @NotNull
  public String getPropertyKey() {
    return propertyKey;
  }

  @NotNull
  public Properties getProperties() {
    return properties;
  }
}
