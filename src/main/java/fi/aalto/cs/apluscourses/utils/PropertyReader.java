package fi.aalto.cs.apluscourses.utils;

import java.util.Properties;
import org.jetbrains.annotations.NotNull;

/**
 * A class that helps reading properties from a {@link Properties} object.
 */
public class PropertyReader {

  @NotNull
  private final Properties properties;

  /**
   * Constructs a reader for {@code properties}.
   *
   * @param properties A {@link Properties} object to be read.
   */
  public PropertyReader(@NotNull Properties properties) {
    this.properties = properties;
  }

  /**
   * Reads a property with key {@code propertyKey} from the properties.
   * The difference between this method and {@code Properties.getProperty(String)} is that this
   * method throws a {@link PropertyException} if the property is not found.
   *
   * @param propertyKey The key of the property.
   * @return The value of the property.
   * @throws PropertyException If the property is not found.
   */
  @NotNull
  public String getProperty(@NotNull String propertyKey) throws PropertyException {
    String value = properties.getProperty(propertyKey);
    if (value == null) {
      throw new PropertyException(properties, propertyKey, "Property was not found.", null);
    }
    return value;
  }

  /**
   * Reads a property and parses its value to an object of type {@code T}.
   *
   * @param propertyKey The key of the property.
   * @param parser      A {@link ValueParser} that parses the value of the property to an object.
   *                    Any runtime exception thrown by the parser is caught and a
   *                    {@link PropertyException} is thrown instead (with the original exception as the
   *                    cause of the {@link PropertyException}).
   * @param <T>         The type of the object to be parsed from the property value.
   * @return An object of type {@code T}.
   * @throws PropertyException If the value cannot be parsed.
   */
  @NotNull
  public <T> T getPropertyAsObject(@NotNull String propertyKey, @NotNull ValueParser<T> parser)
      throws PropertyException {
    String value = getProperty(propertyKey);
    try {
      return parser.parse(value);
    } catch (RuntimeException ex) {
      throw new PropertyException(properties, propertyKey,
          "Value '" + value + "' cannot be parsed", ex);
    }
  }

  @FunctionalInterface
  public interface ValueParser<T> {
    @NotNull
    T parse(@NotNull String value);
  }
}
