package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.ArrayUtil;
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

  @NotNull
  default Path[] findFiles(@NotNull Path directory, @NotNull String[] filenames)
      throws FileDoesNotExistException {
    return ArrayUtil.mapArray(filenames, filename -> findFile(directory, filename), Path[]::new);
  }
}
