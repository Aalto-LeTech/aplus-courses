package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class MissingFileNotification extends Notification {

  @NotNull
  private final Path path;
  @NotNull
  private final String filename;

  /**
   * Construct a missing file notification that explains that a file with the given name couldn't be
   * found in the given module.
   */
  public MissingFileNotification(@NotNull Path path, @NotNull String filename) {
    super(
        PluginSettings.A_PLUS,
        getText("notification.MissingFileNotification.title"),
        getAndReplaceText("notification.MissingFileNotification.content", filename, path),
        NotificationType.ERROR);
    this.path = path;
    this.filename = filename;
  }

  @NotNull
  public Path getPath() {
    return path;
  }

  @NotNull
  public String getFilename() {
    return filename;
  }
}
