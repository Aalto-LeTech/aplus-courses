package fi.aalto.cs.apluscourses.model;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class FileDoesNotExistException extends Exception {

  private static final long serialVersionUID = -8726453899914862488L;
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
