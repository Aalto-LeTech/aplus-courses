package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileSaveViewModel {

  @NotNull
  private final String title;

  @NotNull
  private final String description;

  @Nullable
  private final VirtualFile defaultDirectory;

  @Nullable
  private final String defaultName;

  private Path path;

  /**
   * Construct a view model with the given parameters.
   * @param defaultDirectory An optional default directory.
   * @param defaultName      An optional default name for the file.
   */
  public FileSaveViewModel(@NotNull String title,
                           @NotNull String description,
                           @Nullable VirtualFile defaultDirectory,
                           @Nullable String defaultName) {
    this.title = title;
    this.description = description;
    this.defaultDirectory = defaultDirectory;
    this.defaultName = defaultName;
  }

  @NotNull
  public String getTitle() {
    return title;
  }

  @NotNull
  public String getDescription() {
    return description;
  }

  @Nullable
  public VirtualFile getDefaultDirectory() {
    return defaultDirectory;
  }

  @Nullable
  public String getDefaultName() {
    return defaultName;
  }

  @NotNull
  public Path getPath() {
    return path;
  }

  public void setPath(@NotNull Path path) {
    this.path = path;
  }

}
