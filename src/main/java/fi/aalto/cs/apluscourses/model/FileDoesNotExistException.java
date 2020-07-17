package fi.aalto.cs.apluscourses.model;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileDoesNotExistException extends Exception {
  @NotNull
  private final Path path;
  @NotNull
  private final String name;

  public FileDoesNotExistException(@NotNull Path path,
                                   @NotNull String name) {
    this.path = path;
    this.name = name;
  }

  @NotNull
  public Path getPath() {
    return path;
  }

  @NotNull
  public String getName() {
    return name;
  }
}
