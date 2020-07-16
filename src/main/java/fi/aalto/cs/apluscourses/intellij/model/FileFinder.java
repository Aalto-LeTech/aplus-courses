package fi.aalto.cs.apluscourses.intellij.model;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface FileFinder {
  @Nullable
  Path findFile(@NotNull Path directory, @NotNull String filename);
}
