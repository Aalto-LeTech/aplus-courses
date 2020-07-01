package fi.aalto.cs.apluscourses.model;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileDoesNotExistException extends Exception {
  @NotNull
  private final String name;

  public FileDoesNotExistException(@NotNull String name, @Nullable Throwable cause) {
    super(cause);
    this.name = name;
  }

  @NotNull
  public String getName() {
    return name;
  }
}
