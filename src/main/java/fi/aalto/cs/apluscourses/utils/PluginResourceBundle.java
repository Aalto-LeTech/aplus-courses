package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.project.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;

public class PluginResourceBundle {

  private PluginResourceBundle() {
  }

  private static final ResourceBundle bundle = ResourceBundle.getBundle("resources");

  private static final Map<Project, ResourceBundle> bundles = new HashMap<>();

  public static String getText(@NotNull String key) {
    return bundle.getString(key);
  }

  public static String getText(@NotNull String key, @NotNull Project project) {
    var customText = Optional.ofNullable(bundles.get(project))
        .map(customBundle -> customBundle.getString(key));
    return customText.isEmpty() ? bundle.getString(key) : customText.get();
  }

  public static String getAndReplaceText(@NotNull String key, @NotNull Object... arguments) {
    return MessageFormat.format(PluginResourceBundle.getText(key), arguments);
  }

  public static void setCustomBundle(@NotNull File file, @NotNull Project project) throws IOException {
    try (FileInputStream fis = new FileInputStream(file)) {
      PluginResourceBundle.bundles.put(project, new PropertyResourceBundle(fis));
    }


  }
}
