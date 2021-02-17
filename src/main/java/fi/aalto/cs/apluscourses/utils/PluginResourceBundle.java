package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class PluginResourceBundle {

  private static final ResourceBundle bundle = ResourceBundle.getBundle("resources");

  public static String getText(@NotNull String key) {
    return bundle.getString(key);
  }

  public static String getAndReplaceText(@NotNull String key, @NotNull Object... arguments) {
    String format = MessageFormat.format(PluginResourceBundle.getText(key), arguments);
    return format;
  }
}
