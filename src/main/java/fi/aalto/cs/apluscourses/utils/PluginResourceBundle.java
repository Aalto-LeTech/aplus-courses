package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.project.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginResourceBundle {

  private PluginResourceBundle() {
  }

  private static final ResourceBundle bundle = ResourceBundle.getBundle("resources");

  private static final Map<Project, ResourceBundle> bundles =
      Collections.synchronizedMap(new HashMap<>());

  public static final String CUSTOM_RESOURCES_FILENAME = "customResources.properties";

  public static String getText(@NotNull String key) {
    return getText(key, null);
  }

  /**
   * Gets the text from the project's custom resource bundle if it exists,
   * else gets the default text.
   */
  public static String getText(@NotNull String key, @Nullable Project project) {
    var customText = Optional.ofNullable(bundles.get(project))
        .map(customBundle -> customBundle.getString(key));
    return customText.isEmpty() ? bundle.getString(key) : customText.get();
  }

  public static String getAndReplaceText(@NotNull String key, @NotNull Object... arguments) {
    return getAndReplaceText(key, null, arguments);
  }

  /**
   * Gets and replaces the text from the project's custom resource bundle if it exists,
   * else gets the default text.
   */
  public static String getAndReplaceText(@NotNull String key, @Nullable Project project,
                                         @NotNull Object... arguments) {
    return MessageFormat.format(PluginResourceBundle.getText(key, project), arguments);
  }

  /**
   * Sets a custom bundle for a project.
   */
  public static void setCustomBundle(@NotNull File file,
                                     @NotNull Project project) throws IOException {
    try (var fis = new FileInputStream(file)) {
      PluginResourceBundle.bundles.put(project, new PropertyResourceBundle(fis));
    }


  }
}
