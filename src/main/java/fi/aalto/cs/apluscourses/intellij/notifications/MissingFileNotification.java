package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

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
        "A+",
        "Could not find file",
        "A+ Courses plugin couldn't find the file " + filename + " in directory " + path
            + ". Please double-check from which module you intend to submit.",
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
