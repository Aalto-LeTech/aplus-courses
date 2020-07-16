package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.model.FileFinder;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class SubmittableFile {
  @NotNull
  private final String name;

  public SubmittableFile(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public Path getPath(Path basePath, FileFinder fileFinder)
      throws FileDoesNotExistException {
    return Optional.ofNullable(fileFinder.findFile(basePath, name))
        .orElseThrow(() -> new FileDoesNotExistException(name, null));
  }

  public String getName() {
    return name;
  }
}
