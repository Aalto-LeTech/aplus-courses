package fi.aalto.cs.apluscourses.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;

public class PluginResourceBundle {

  private PluginResourceBundle() {
  }

  private static final ResourceBundle bundle = ResourceBundle.getBundle("resources");

  public static String getText(@NotNull String key) {
    return bundle.getString(key);
  }

  public static String getAndReplaceText(@NotNull String key, @NotNull Object... arguments) {
    return MessageFormat.format(PluginResourceBundle.getText(key), arguments);
  }
}
