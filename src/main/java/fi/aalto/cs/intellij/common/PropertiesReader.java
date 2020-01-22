package fi.aalto.cs.intellij.common;

import java.util.Properties;
import org.jetbrains.annotations.NotNull;

class PropertiesReader {
  private Properties properties;

  public PropertiesReader(Properties properties) {
    this.properties = properties;
  }

  public String getProperty(@NotNull String propertyKey) throws PropertyException {
    String value = properties.getProperty(propertyKey);
    if (value == null) {
      throw new PropertyException("'" + propertyKey + "' is missing from the build info.");
    }
    return value;
  }

  public static class PropertyException extends Exception {
    public PropertyException(@NotNull String message) {
      super(message);
    }
  }
}
