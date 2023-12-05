package fi.aalto.cs.apluscourses.model;

import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface FileFinder {

  @Nullable
  Path tryFindFile(@NotNull Path directory, @NotNull String filename);

  @NotNull
  default Path findFile(@NotNull Path directory, @NotNull String filename)
      throws FileDoesNotExistException {
    return Optional.ofNullable(tryFindFile(directory, filename))
        .orElseThrow(() -> new FileDoesNotExistException(directory, filename));
  }

  /**
   * Resolves an array of filenames to an array of Paths.
   */
  @NotNull
  default Path[] findFiles(@NotNull Path directory, @NotNull String[] filenames)
      throws FileDoesNotExistException {
    Path[] paths = new Path[filenames.length];
    for (int i = 0; i < filenames.length; i++) {
      paths[i] = findFile(directory, filenames[i]);
    }
    return paths;
  }
}
